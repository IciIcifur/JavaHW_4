public class Main {
    static final String TITLE_OF_PROGRAM = "Elevators Simulation";
    static final int WINDOW_WIDTH = 650, WINDOW_HEIGHT = 700;
    static final int FLOORS = 8, MAXIMUM_PEOPLE_ON_FLOOR = 10;

    public static void main(String[] args) {
        Thread houseThread = new Thread(new Skyscraper(WINDOW_WIDTH, WINDOW_HEIGHT, TITLE_OF_PROGRAM, FLOORS, MAXIMUM_PEOPLE_ON_FLOOR));
        houseThread.start();
    }
}