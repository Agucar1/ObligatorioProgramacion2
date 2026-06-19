package uy.edu.um.doors;

/*import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;*/

/*@Setter
@Getter
@NoArgsConstructor*/
public class Users {
    private int uid;
    private String alias;
    private String type;

    public Users() {

    }

    public int getUid() {
        return uid;
    }

    public void setUid(int uid) {
        this.uid = uid;
    }

    public String getAlias() {
        return alias;
    }

    public void setAlias(String alias) {
        this.alias = alias;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }
}
