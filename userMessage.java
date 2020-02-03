  import java.text.SimpleDateFormat;
  import java.util.Date;
  
  public class userMessage {
  public String date="";
  private String message="";
  private boolean isCurrentUser=true;
  
  public userMessage(String message, boolean isCurrentUser){
  this.message=message;
  this.isCurrentUser = isCurrentUser;
  this.date=getDate();
  }
  public userMessage(String message, boolean isCurrentUser, String date){
  this.message=message;
  this.isCurrentUser = isCurrentUser;
  this.date=date;
  }
  public String getCombinedMessage(){
  return isCurrentUser+"\t"+date+"\t"+message;
  }
  public String getMessage(){
  return message;
  }
  public boolean getUser()
  {
      return isCurrentUser;
  }
  
  public String getDate(){
    String date = new SimpleDateFormat("MMMM, dd hh:mm a").format(new Date());
    return date; 
  }
  }
