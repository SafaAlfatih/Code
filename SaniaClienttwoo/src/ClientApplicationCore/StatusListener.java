package ClientApplicationCore;

/**
A listener that can be updated on a session's status.
*/
public interface StatusListener {
  public void notify(int status);
}
