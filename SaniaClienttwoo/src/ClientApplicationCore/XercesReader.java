package ClientApplicationCore;

import java.io.*;

public class XercesReader extends FilterReader {

  public XercesReader(InputStream in){
    super(new InputStreamReader(in));
  }

  private XercesReader(Reader in){
    super(in);
  }
  int sendBlank = 0;

  public int read() throws IOException {
    if (sendBlank > 0) {
      sendBlank--;
      return (int)' ';
    }
    int b = in.read();
    if (b == (int)'>'){
      sendBlank = 2;
    }
    return b;
  }

  public int read(char [] text, int offset, int length) throws IOException {
    int numRead = 0;
    for (int i = offset; i < offset + length; i++){
      int temp = this.read();
      if (temp == -1) break;
      text[i] = (char) temp;
      numRead++;
    }
    if (numRead == 0 && length != 0) numRead = -1;
    return numRead;
  }
  /*
  public int read(char [] text, int offset, int length) throws IOException {
    if (length == 0) return 0;
    int readInt;
    int numRead = 0;
    if (!in.read()){
      readInt = this.iread();
      if (readInt == -1) {
        return readInt;
      } else {
        text[offset] = (char) readInt;
        numRead=1;
      }
    }
    for (int i = offset + numRead; i < offset + length && in.ready(); i++){
      int readInt = this.iread();
      if (readInt == -1) break;
      text[i] = (char) readInt;
      numRead++;
    }
    if (numRead == 0 && !in.ready()) numRead = -1;
    else
    System.out.println("---------------- read[" + numRead + "/" + (in.ready() ? "t" : "f") + "] " + new String(text,offset,numRead));
    return numRead;
  }
    */
}
