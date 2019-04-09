import org.apache.hadoop.io.Text;

/**
 * Created by lyc08 on 2016/11/4.
 */
public class RecordParse {

    private static final String PARENTID= "ParentId";
    private static final String POSTTYPEID = "PostTypeId";
    private static final String OWNERUSERID = "OwnerUserId";

    private String parentid;
    private String postTypeId;
    private String userid;

    public void parse(String record) {

        // find post type
        int index = record.indexOf(POSTTYPEID);
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

        // find parentid
        index = record.indexOf(PARENTID);
        if( index != -1){
            int start = record.substring(index).indexOf('"');
            int end = record.substring(index).indexOf('"', start+1);
            parentid = record.substring(index).substring(start+1, end);
        } else
            parentid = null;
    }
    public void parse(Text record) {
        parse(record.toString());
    }

    public String getUserid() {
        return userid;
    }

    public String getParentid() {
        return parentid;
    }

    public boolean isAnswer(){
        return "2".equals(postTypeId);
    }
}
