#include "ns3/core-module.h"
#include "ns3/propagation-module.h"
#include "ns3/network-module.h"
#include "ns3/applications-module.h"
#include "ns3/mobility-module.h"
#include "ns3/internet-module.h"
#include "ns3/flow-monitor-module.h"
#include "ns3/wifi-module.h"
#include "ns3/random-variable-stream.h"

#include <string>
#include <stdio.h>

using namespace ns3;

NS_LOG_COMPONENT_DEFINE ("Assignment1");

void experiment(int seed, double distance, bool aarf, bool rayleigh)
{

  std::string algo = (aarf==true?"AARF":"CARA");
  std::string fading = (rayleigh==true?"With":"No");
  NS_LOG_UNCOND("----------------------------");
  NS_LOG_UNCOND("  " << algo << "-" << fading << "Fading");
  NS_LOG_UNCOND("  Distance: " << distance << ", Seed:" << seed);
  double simuTime = 10.0;
  
  // 0.Set random seeds
  SeedManager::SetSeed (seed);//seed number should change between runs if running multiple simulations.

  // 1. Create nodes
  NodeContainer wifiStaNodes; //create AP Node and (one or more) Station node(s)
  wifiStaNodes.Create (1);
  NodeContainer wifiApNode;
  wifiApNode.Create(1);

  // 2. Set Channel and propagation loss
  YansWifiChannelHelper channel; //create helpers for the channel and phy layer and set propagation configuration here.
  channel.SetPropagationDelay ("ns3::ConstantSpeedPropagationDelayModel");
  channel.AddPropagationLoss("ns3::LogDistancePropagationLossModel");
  if (rayleigh){
	  //combining log and nakagami to have both distance and rayleigh fading (nakami with m0, m1 and m2 =1 is rayleigh)
	  channel.AddPropagationLoss("ns3::NakagamiPropagationLossModel",
	                           "m0", DoubleValue(1.0), "m1", DoubleValue(1.0), "m2", DoubleValue(1.0));
  }
  // 3. Install wireless devices
  WifiHelper wifi = WifiHelper::Default (); //create helper for the overall wifi setup and configure a station manager
  wifi.SetStandard(ns3::WIFI_PHY_STANDARD_80211g);
  if(aarf)
  {
	  wifi.SetRemoteStationManager ("ns3::AarfWifiManager");
  }
  else 
  {
	  wifi.SetRemoteStationManager ("ns3::CaraWifiManager");
  }
  YansWifiPhyHelper phy = YansWifiPhyHelper::Default ();
  phy.SetChannel (channel.Create ());
  NqosWifiMacHelper mac = NqosWifiMacHelper::Default (); //create a mac helper and configure for station and AP and install

  Ssid ssid = Ssid ("example-ssid");
  mac.SetType ("ns3::StaWifiMac",
               "Ssid", SsidValue (ssid),
               "ActiveProbing", BooleanValue (false));

  NetDeviceContainer staDevices;
  staDevices = wifi.Install (phy, mac, wifiStaNodes);

  mac.SetType ("ns3::ApWifiMac",
               "Ssid", SsidValue (ssid));

  NetDeviceContainer apDevices;
  apDevices = wifi.Install (phy, mac, wifiApNode);

  // 4. Place Nodes
  MobilityHelper mobility;
  Ptr<ListPositionAllocator> positionAlloc = CreateObject<ListPositionAllocator> ();
  mobility.SetMobilityModel ("ns3::ConstantPositionMobilityModel");
  //Set position for APs
  positionAlloc->Add (Vector (0.0, 0.0, 0.0));  
  positionAlloc->Add (Vector (distance, 0.0, 0.0));
  mobility.SetPositionAllocator (positionAlloc);
  mobility.Install (wifiApNode);
  mobility.Install (wifiStaNodes);

  // 5. Install stack & assign IP address
  InternetStackHelper stack; //install the internet stack on both nodes
  stack.Install (wifiApNode);
  stack.Install (wifiStaNodes);

  Ipv4AddressHelper address; //assign IP addresses to all nodes
  address.SetBase ("10.1.1.0", "255.255.255.0");
  address.Assign (staDevices);
  Ipv4InterfaceContainer apAddress = address.Assign (apDevices); //we need to keep AP address accessible as we need it later

  // 6. Install applications
  OnOffHelper onoff ("ns3::UdpSocketFactory", Address ()); //create a new on-off application to send data
  std::string dataRate = "20Mib/s"; //data rate set as a string, see documentation for accepted units
  onoff.SetConstantRate(dataRate, (uint32_t)1024); //set the onoff client application to CBR mode

  AddressValue remoteAddress (InetSocketAddress (apAddress.GetAddress (0), 8000)); //specify address and port of the AP as the destination for on-off application's packets
  onoff.SetAttribute ("Remote", remoteAddress);

  ApplicationContainer apps = onoff.Install (wifiStaNodes);//install onoff application on stanode 0 and configure start/stop times

  Ptr<UniformRandomVariable> var = CreateObject<UniformRandomVariable> ();
  apps.Start(Seconds(var->GetValue(0, 0.1)));
  apps.Stop (Seconds (simuTime));

  PacketSinkHelper sink("ns3::UdpSocketFactory", InetSocketAddress (apAddress.GetAddress (0), 8000)); //create packet sink on AP node with address and port that on-off app is sending to
  apps.Add(sink.Install(wifiApNode.Get(0)));
  
  // 7. Install FlowMonitor on all nodes
  FlowMonitorHelper flowmon;
  Ptr<FlowMonitor> monitor = flowmon.InstallAll ();

  // 8. Run sumulation for 10 seconds
  Simulator::Stop (Seconds (simuTime)); //define stop time of simulator
  Simulator::Run ();

  // 9. Print per flow statistics
  monitor->CheckForLostPackets ();
  Ptr<Ipv4FlowClassifier> classifier = DynamicCast<Ipv4FlowClassifier> (flowmon.GetClassifier ());
  FlowMonitor::FlowStatsContainer stats = monitor->GetFlowStats ();
  for (std::map<FlowId, FlowMonitor::FlowStats>::const_iterator i = stats.begin (); i != stats.end (); ++i)
    {
     
      //Ipv4FlowClassifier::FiveTuple t = classifier->FindFlow (i->first);
      //std::cout << "  Flow " << i->first - 2 << " (" << t.sourceAddress << " -> " << t.destinationAddress << ")\n";
      //std::cout << "  Tx Packets: " << i->second.txPackets << "\n";
      //std::cout << "  Tx Bytes:   " << i->second.txBytes << "\n";
      //std::cout << "  TxOffered:  " << i->second.txBytes * 8.0 / simuTime / 1000 / 1000  << " Mbps\n";
      //std::cout << "  Rx Packets: " << i->second.rxPackets << "\n";
      //std::cout << "  Rx Bytes:   " << i->second.rxBytes << "\n";
      //std::cout << "  Throughput: " << i->second.rxBytes * 8.0 / simuTime / 1000 / 1000  << " Mbps\n";
      NS_LOG_UNCOND("  Throughput: " << i->second.rxBytes * 8.0 / simuTime / 1000 / 1000  << " Mbps\n");
    }

  // 10. Cleanup
  Simulator::Destroy ();
}

int main (int argc, char **argv)
{
  CommandLine cmd;
  cmd.Parse (argc, argv);
  
  int seed=1;
  
  
  // AARF with No Fading
  for (int d = 1; d <= 20; d++)
  {
	  for(int i = 1; i <= 5; i++){
		  experiment (seed, d * 5.0, true, false);	
		  seed++;
	  }
  }
  NS_LOG_UNCOND("***********************");
  
  seed=1;
  // AARF with Fading
  for (int d = 1; d <= 20; d++)
  {
	  for(int i = 1; i <= 5; i++){
		  experiment (seed, d * 5.0, true, true);	
		  seed++;
	  }
  }
  NS_LOG_UNCOND("***********************");
  
  seed=1;
  // CARA with No Fading
  for (int d = 1; d <= 20; d++)
  {
	  for(int i = 1; i <= 5; i++){
		  experiment (seed, d * 5.0, false, false);	
		  seed++;
	  }
  }
  NS_LOG_UNCOND("***********************");
  
  seed=1;
  // CARA with Fading
  for (int d = 1; d <= 20; d++)
  {
	  for(int i = 1; i <= 5; i++){
		  experiment (seed, d * 5.0, false, true);	
		  seed++;
	  }
  }
  
  return 0;
}
