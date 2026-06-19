package uy.edu.um.doors;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
public class Users {
    private int uid;
    private String alias;
    private String type;
}