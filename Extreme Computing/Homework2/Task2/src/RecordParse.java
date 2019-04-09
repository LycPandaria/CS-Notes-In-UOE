import org.apache.hadoop.io.Text;

/**
 * Created by lyc08 on 2016/11/4.
 */
public class RecordParse {

    private static final String VIEWCOUNTSTR= "ViewCount";
    private static final String IDSTR = "Id";
    private static final String POSTTYPEID = "PostTypeId";

    private String id;
    private Long count;
    private String postTypeId;

    public void parse(String record) {
        // find view count
        int index = record.indexOf(VIEWCOUNTSTR);
        if( index != -1){
            int start = record.substring(index).indexOf('"');
            int end = record.substring(index).indexOf('"', start+1);
            count = Long.parseLong(record.substring(index).substring(start+1, end));
        } else
            count = -1L;

        // find id
        index = record.indexOf(IDSTR);
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
    }
    public void parse(Text record) {
        parse(record.toString());
    }

    public String getId() {
        return id;
    }

    public Long getCount() {
        return count;
    }

    public String getPostTypeId() {
        return postTypeId;
    }

    public boolean isQuetion(){
        return "1".equals(postTypeId);
    }
}
