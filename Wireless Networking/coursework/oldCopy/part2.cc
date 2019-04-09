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

NS_LOG_COMPONENT_DEFINE ("Assignment2");

void experiment(int seed, int nStas, bool aarf)
{

  std::string algo = (aarf==true?"AARF":"CARA");

  NS_LOG_UNCOND("----------------------------");
  NS_LOG_UNCOND("  " << algo << " With " << nStas << " stations in Seed:" << seed);
  double simuTime = 10.0;
  
  // 0.Set random seeds
  SeedManager::SetSeed (seed);//seed number should change between runs if running multiple simulations.

  // 1. Create nodes
  NodeContainer wifiStaNodes; //create AP Node and (one or more) Station node(s)
  wifiStaNodes.Create (nStas);
  NodeContainer wifiApNode;
  wifiApNode.Create(1);

  // 2. Set Channel and propagation loss
  YansWifiChannelHelper channel; //create helpers for the channel and phy layer and set propagation configuration here.
  channel.SetPropagationDelay ("ns3::ConstantSpeedPropagationDelayModel");
  channel.AddPropagationLoss("ns3::LogDistancePropagationLossModel");
  
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
  // Node place around a circle
	srand((unsigned)time(NULL));// Set random seed
  for(int i = 0; i < nStas; i++)
  {
	// compute deg
	double deg = rand()%360 / 180.0 * 3.1415926;
	// set location
	positionAlloc->Add(Vector(10 * sin(deg), 10 * cos(deg), 0.0));
	//NS_LOG_UNCOND(10 * sin(deg)<<","<<10 * cos(deg));			
  }
  
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
  Ptr<UniformRandomVariable> var = CreateObject<UniformRandomVariable> ();
  
  //create packet sink on AP node with address and port that on-off app is sending to
  PacketSinkHelper sink("ns3::UdpSocketFactory", InetSocketAddress (apAddress.GetAddress (0), 8000)); 
  
  ApplicationContainer sinkApp = sink.Install (wifiApNode.Get (0));
  sinkApp.Start (Seconds (0.0));
  sinkApp.Stop (Seconds (simuTime));
  
  OnOffHelper onoff ("ns3::UdpSocketFactory", Address ()); //create a new on-off application to send data
  std::string dataRate = "20Mib/s"; //data rate set as a string, see documentation for accepted units
  onoff.SetConstantRate(dataRate, (uint32_t)1024); //set the onoff client application to CBR mode

  AddressValue remoteAddress (InetSocketAddress (apAddress.GetAddress (0), 8000)); //specify address and port of the AP as the destination for on-off application's packets
  onoff.SetAttribute ("Remote", remoteAddress);

  ApplicationContainer apps = onoff.Install (wifiStaNodes);//install onoff application on stanode 0 and configure start/stop times

  apps.Start(Seconds(var->GetValue(0, 0.1)));
  apps.Stop (Seconds (simuTime));

  apps.Add(sinkApp);
  
  // 7. Install FlowMonitor on all nodes
  //FlowMonitorHelper flowmon;
  //Ptr<FlowMonitor> monitor = flowmon.InstallAll ();

  // 8. Run sumulation for 10 seconds
  Simulator::Stop (Seconds (simuTime)); //define stop time of simulator
  Simulator::Run ();

  // 9. Print per flow statistics
  
  uint32_t totalPacketsThrough = DynamicCast<PacketSink> (sinkApp.Get (0))->GetTotalRx ();
  double throughput = totalPacketsThrough * 8 / (simuTime * 1000000.0);
  NS_LOG_UNCOND( "Throughput: " << throughput << " Mbps"); 
	
	// 10. Cleanup
  Simulator::Destroy ();
}

int main (int argc, char **argv)
{
  CommandLine cmd;
  cmd.Parse (argc, argv);
  
  int seed=1;
  experiment(seed, 5, true);
  experiment(2, 5, true);
  /*
  for(int i = 0; i < 5; i++)
	  {
		  experiment(seed, 1, true);
		  seed++;
	  }  
  for(int nStas = 5; nStas <= 50; nStas+=5)
  {
	  for(int i = 0; i < 5; i++)
	  {
		  experiment(seed, nStas, true);
		  seed++;
	  }  
  }
  
  
  NS_LOG_UNCOND("****************************");
  seed = 1;
  for(int i = 0; i < 5; i++)
	  {
		  experiment(seed, 1, false);
		  seed++;
	  }
  for(int nStas = 5; nStas <= 50; nStas+=5)
  {
	  for(int i = 0; i < 5; i++)
	  {
		  experiment(seed, nStas, false);
		  seed++;
	  }  
  }
  */
  return 0;
}
