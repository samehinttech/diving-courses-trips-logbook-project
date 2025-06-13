package ch.oceandive.dto;

/**
 * Custom response object for API responses been used for debugging to get a better overview of the response.
 */
public class Response {
    private boolean success;
    private String message;
    private Object data;
    
    public Response() {
    }
    
    public Response(boolean success, String message, Object data) {
        this.success = success;
        this.message = message;
        this.data = data;
    }
    
    // Getters and Setters
    public boolean isSuccess() {
        return success;
    }
    
    public void setSuccess(boolean success) {
        this.success = success;
    }
    
    public String getMessage() {
        return message;
    }
    
    public void setMessage(String message) {
        this.message = message;
    }
    
    public Object getData() {
        return data;
    }
    
    public void setData(Object data) {
        this.data = data;
    }
}