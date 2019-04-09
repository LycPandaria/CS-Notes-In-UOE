import org.apache.hadoop.io.Text;

/**
 * Created by lyc08 on 2016/11/4.
 */
public class RecordParse {

    private static final String IDSTR = "Id";
    private static final String POSTTYPEID = "PostTypeId";
    private static final String OWNERUSERID = "OwnerUserId";
    private static final String ACCEPTEDANSWERID =  "AcceptedAnswerId";

    private String id;
    private String acceptedAnsweId;
    private String postTypeId;
    private String userid;

    public void parse(String record) {

        // find id
        int index = record.indexOf(IDSTR);
        if( index != -1){
            int start = record.substring(index).indexOf('"');
            int end = record.substring(index).indexOf('"', start+1);
            id = record.substring(index).substring(start+1, end);
        } else
            id = null;

        // find post type
        index = record.indexOf(POSTTYPEID);
        if( index != -1){
            int start = record.substring(index).indexOf('"');
            int end = record.substring(index).indexOf('"', start+1);
            postTypeId = record.substring(index).substring(start+1, end);
        } else
            postTypeId = null;

        // find user id
        index = record.indexOf(OWNERUSERID);
        if( index != -1){
            int start = record.substring(index).indexOf('"');
            int end = record.substring(index).indexOf('"', start+1);
            userid = record.substring(index).substring(start+1, end);
        } else
            userid = null;

        // find acceptedAnswerId
        index = record.indexOf(ACCEPTEDANSWERID);
        if( index != -1){
            int start = record.substring(index).indexOf('"');
            int end = record.substring(index).indexOf('"', start+1);
            acceptedAnsweId = record.substring(index).substring(start+1, end);
        } else
            acceptedAnsweId = null;
    }
    public void parse(Text record) {
        parse(record.toString());
    }

    public String getId() {
        return id;
    }

    public String getPostTypeId() {
        return postTypeId;
    }

    public String getUserid() {
        return userid;
    }

    public boolean isAnswer(){
        return "2".equals(postTypeId);
    }

    public boolean isQuestion() {
        return "1".equals(postTypeId);
    }

    public String getAcceptedanswerid() {
        return acceptedAnsweId;
    }
}
