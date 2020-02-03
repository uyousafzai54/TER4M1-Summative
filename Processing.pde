/*
TER Summative Project
Max Belleville and Umar Yousafzai
June 17th, 2019
*/
import processing.serial.*;
import controlP5.*;
ControlP5 cp5;

Serial myPort;  // Create object from Serial class
ArrayList<userMessage> messages = new ArrayList<userMessage>();  
Textfield field;
PGraphics textBox = new PGraphics();
PVector textBoxPos = new PVector(20,20);
int scroll = 60;
void setup()
{
  size(900,600);
  background(255);
 
  
  
  loadFromFile();
      
  String portName = Serial.list()[Serial.list().length-1];
  myPort = new Serial(this, portName, 9600);
  
  PFont font = createFont("arial",20);
  
  cp5 = new ControlP5(this);
  field = cp5.addTextfield("")
     .setPosition(10,height-34)
     .setSize(width-60,26)
     .setFont(font)
     .setAutoClear(false)
     .setFocus(true)
     ;
  field.setColorBackground(color(255));
  field.setColorForeground(color(255));
  field.setColorActive(color(255));
  field.setColorCursor(color(0));
  field.setColor(color(0));
  cp5.addBang("Send")
     .setPosition(width-48,height-32)
     .setSize(24,24)
     .setImage(loadImage("send-24px.png"))
     .setColorForeground(color(255))
     .setColorActive(color(255))
     .setLabelVisible(false)
     ;    
   
 
  image(textBox, textBoxPos.x, textBoxPos.y); 
    textFont(font);
}
void saveToFile()
{
  String[] tempCombinedMessages = new String[messages.size()];
  for(int i=0; i<messages.size(); i++)
    tempCombinedMessages[i] = messages.get(i).getCombinedMessage();

  saveStrings("messages.txt", tempCombinedMessages);
}
void loadFromFile()
{
  try{
  String[] tempCombinedMessages = loadStrings("messages.txt");
  
  for(int i=0; i<tempCombinedMessages.length; i++)
  {
    String[] parts = tempCombinedMessages[i].split("\t");
    if(parts.length>=3){
    boolean val = Boolean.parseBoolean(parts[0]);
    messages.add(new userMessage(parts[2],val, parts[1]));
    }
  }
  }
  catch (NullPointerException error)
{
  print("no file availible");
}
}
void keyPressed() {
if(keyCode==DOWN&&messages.size()*scroll<height*2){
scroll+=5;
}
if(keyCode==UP&&messages.size()*scroll>-height){
scroll-=5;
}
}
void readFromArduino()
{
  String val;
  if(myPort.available()>0)
  { //If data is available
    val = myPort.readStringUntil('\n');
    if(val!=null){
      if(!val.trim().isEmpty()){
      print("Message: "+val);
      messages.add(new userMessage(val, false));
      }
    }
  }
}
void draw() 
{
 background(255);
  displayMessages(); 
 stroke(1);
 line(10,height-8,width-60,height-8);
 readFromArduino();
}

void exit() {
 super.exit();
 saveToFile();
}

void displayMessages()
{
  int shiftAmount = scroll;
  fill(0);
  for(int i=messages.size()-1; i>-1; i--)
  {
    int xc = 285;
    if(messages.get(i).getUser()==true)
      {fill(255, 0, 0);
      text("User (You): ", 175, height-shiftAmount);
    }
    else
    {fill(0, 128, 255);
    text("Arduino: ",175, height-shiftAmount);
     xc-=29;
   }
    fill(0);
    String date = messages.get(i).date;
    text(date, 0, height-shiftAmount);
    if(messages.get(i).getMessage().length()>68)
   {
     text(messages.get(i).getMessage().substring(0,68), xc, height-shiftAmount);
     text(messages.get(i).getMessage().substring(68, messages.get(i).getMessage().length()), xc, height-shiftAmount+20);
   }
   else
    {text(messages.get(i).getMessage(), xc, height-shiftAmount);}
    shiftAmount+=50;
  }
 noStroke();
  fill(255,255,255);
  rect(0,height-60,width,height);
} 
void controlEvent(ControlEvent theEvent) {
      messages.add(new userMessage(field.getText(),true));
      println(field.getText());  
      myPort.write(field.getText());
      field.clear();
      saveToFile();
      
}
