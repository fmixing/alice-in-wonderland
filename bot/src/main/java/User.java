import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

import java.util.Set;

@JsonIgnoreProperties(ignoreUnknown = true)
public class User {
    Long userID;

    Set<Long> postedDrives;

    Set<Long> joinedDrives;

    public Long getUserID() {
        return userID;
    }

    public void setUserID(Long userID) {
        this.userID = userID;
    }

    public Set<Long> getPostedDrives() {
        return postedDrives;
    }

    public void setPostedDrives(Set<Long> postedDrives) {
        this.postedDrives = postedDrives;
    }

    public Set<Long> getJoinedDrives() {
        return joinedDrives;
    }

    public void setJoinedDrives(Set<Long> joinedDrives) {
        this.joinedDrives = joinedDrives;
    }

    public String toString() {
        StringBuilder toStr = new StringBuilder("User{" +
                "userID=" + userID);
        if (!joinedDrives.isEmpty()) {
            toStr.append(";joinedDrives=");
            joinedDrives.forEach(v -> toStr.append(v).append(" "));
        }
        if (!postedDrives.isEmpty()) {
            toStr.append(";postedDrives=");
            postedDrives.forEach(v -> toStr.append(v).append(" "));
        }
        toStr.append('}');
        return toStr.toString();
    }
}
