package ClientApplicationCore;

class RosterHandler implements PacketListener {

  // handle incoming presence and roster packets
    /**
     *
     * @param packet
     */
    @Override
  public void notify(Packet packet) {
    System.out.print("roster: ");
    System.out.println(packet.toString());
    System.out.println("Siddiq testing the Handler ");
    
  }
}