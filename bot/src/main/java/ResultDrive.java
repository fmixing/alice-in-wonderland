import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

@JsonIgnoreProperties(ignoreUnknown = true)

public class ResultDrive {

    Drive jsonDrive;

    String message;

    public boolean hasMessage() {
        if (message != null)
            return true;
        return false;
    }

    public boolean hasResult() {
        if (jsonDrive != null)
            return true;
        return false;
    }

    public Drive getJsonDrive() {
        return jsonDrive;
    }

    public void setJsonDrive(Drive jsonDrive) {
        this.jsonDrive = jsonDrive;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    @Override
    public String toString() {
        return "Result{" +
                "jsonDrive=" + jsonDrive +
                '}';
    }
}
