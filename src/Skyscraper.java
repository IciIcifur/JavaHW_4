import javax.swing.*;
import java.awt.*;
import java.util.ArrayList;
import java.util.ConcurrentModificationException;
import java.util.Random;

public class Skyscraper extends JFrame implements Runnable {
    // house params
    static int width = 400, height;
    static int floorHeight = 76, floorsNumber;
    static int x1, x2, y1, y2;
    static int attic = 24, elevatorsBoxWidth = 155, elevatorsBoxX1, elevatorsBoxX2;
    // people
    static final ArrayList<ArrayList<Person>> peopleOnFloors = new ArrayList<>();
    static final ArrayList<Person> disappearing = new ArrayList<>();
    static final ArrayList<Integer> queue = new ArrayList<>();
    static int maximumPeopleOnFloor;
    // elevators
    volatile static Elevator[] elevators;
    // window
    static Canvas canvas;
    static int WINDOW_WIDTH;
    static int WINDOW_HEIGHT;
    /**
     * Defines number of ms before new frame.
     */
    static int MSpF;
    // other
    static Random random = new Random();

    public Skyscraper(int w, int h, String title, int fps, int floorsNumber, int maximumPeopleOnFloor) {
        // canvas settings
        MSpF = 1000 / fps;
        WINDOW_HEIGHT = h;
        WINDOW_WIDTH = w;
        setTitle(title);
        setDefaultCloseOperation(EXIT_ON_CLOSE);

        Skyscraper.canvas = new Canvas();
        canvas.setBackground(Color.white);
        canvas.setPreferredSize(new Dimension(WINDOW_WIDTH, WINDOW_HEIGHT));

        add(canvas);
        pack();
        setLocationRelativeTo(null);
        setResizable(false);
        setVisible(true);

        // house settings
        Skyscraper.floorsNumber = floorsNumber;
        height = floorHeight * floorsNumber + attic;
        Skyscraper.maximumPeopleOnFloor = maximumPeopleOnFloor;

        x1 = (WINDOW_WIDTH - Skyscraper.width) / 2;
        x2 = x1 + width;
        y2 = (WINDOW_HEIGHT - 10);
        y1 = y2 - height;

        elevatorsBoxX1 = x1 + (width - elevatorsBoxWidth) / 2;
        elevatorsBoxX2 = x1 + (width - elevatorsBoxWidth) / 2 + elevatorsBoxWidth;

        // people
        for (int i = 1; i <= floorsNumber + 1; i++) {
            peopleOnFloors.add(new ArrayList<>());
        }

        // elevators settings
        elevators = new Elevator[2];
        elevators[0] = new Elevator(elevatorsBoxX1 + 10, y2 - Elevator.height, Elevator.Size.LARGE);
        elevators[1] = new Elevator(elevatorsBoxX2 - 10 - Elevator.Size.SMALL.width, y2 - Elevator.height, Elevator.Size.SMALL);
    }

    public void run() {
        // starting lifts in new threads
        Thread eSmall = new Thread(new ElevatorActive(0));
        Thread eLarge = new Thread(new ElevatorActive(1));
        // lifts are less prioritized than the house itself
        eSmall.setPriority(4);
        eLarge.setPriority(4);

        eSmall.start();
        eLarge.start();

        while (true) {
            canvas.repaint();
            try {
                Thread.sleep(MSpF);
                newPerson(); // generating new orders and people on floors
                animate(); // animating people on floors
            } catch (InterruptedException | ConcurrentModificationException e) {
                System.out.println(e.getMessage());
                System.out.println(Thread.currentThread() + " finishes its work.");
                break;
            }
            if (!eLarge.isAlive()) {
                System.out.println("Restarting large elevator...");
                eLarge.start();
            }
            if (!eSmall.isAlive()) {
                System.out.println("Restarting small elevator...");
                eSmall.start();
            }
        }
    }

    /**
     * Spawns people on floors (less than 2% chance per MSpF ms)
     */
    void newPerson() {
        int currentFloor = 1 + random.nextInt(floorsNumber);
        int destinationFloor = 1 + random.nextInt(floorsNumber);
        synchronized (peopleOnFloors.get(currentFloor)) {
            if (peopleOnFloors.get(currentFloor).size() < maximumPeopleOnFloor
                    && currentFloor != destinationFloor
                    && random.nextInt(100) < 2) { // 2% chance to spawn a person
                int currentY = y1 + 1 + attic + (floorsNumber - currentFloor + 1) * floorHeight - Person.height;
                int currentX = x1 + 1 + random.nextInt(width - Person.width);
                peopleOnFloors.get(currentFloor).add(new Person(destinationFloor, currentX, currentY));
                addOrder(currentFloor);
            }
        }
    }

    /**
     * Adds an order in the queue if necessary.
     */
    static void addOrder(int currentFloor) {
        synchronized (queue) {
            for (Integer integer : queue) {
                if (integer == currentFloor) return;
            }
            queue.add(currentFloor);
        }
    }

    /**
     * Allows people on house's floors to move.
     */
    void animate() {
        synchronized (peopleOnFloors) {
            for (ArrayList<Person> floor : peopleOnFloors) {
                for (int i = 0; i < floor.size(); i++)
                    floor.get(i).go(x1, x2);
            }
        }

        ArrayList<Person> disappeared = new ArrayList<>();
        synchronized (disappearing) {
            for (Person person : disappearing) {
                if (person.disappear(person.destinationFloor == 1 ?
                        0 : x1, person.destinationFloor == 1 ? WINDOW_WIDTH : x2)) {
                    disappeared.add(person);
                }
            }
            disappearing.removeAll(disappeared);
        }
    }

    /**
     * Paints background and house.
     */
    static void paint(Graphics context, int windowH, int windowW) {
        // sky
        context.setColor(new Color(227, 244, 255));
        context.fillRect(0, 0, windowW, windowH);
        // grass
        context.setColor(new Color(204, 225, 153));
        context.fillRect(0, windowH - 20, windowW, 20);
        // house
        context.setColor(Color.white);
        context.fillRect(x1, y1, width, height);
        // elevators' box
        context.setColor(new Color(244, 244, 244));
        context.fillRect(elevatorsBoxX1, y1, elevatorsBoxWidth, height);
        // floors
        int floorX1 = x1;
        int floorX2 = x1 + width;
        for (int i = 1; i <= floorsNumber; i++) {
            int floorH = y1 + attic + i * floorHeight;
            int currentFloor = floorsNumber - i + 1;
            context.setColor(Color.black);
            context.drawString(String.valueOf(currentFloor), floorX1 - 20, floorH - 15);
            context.drawLine(floorX1, floorH, floorX2, floorH);
        }
        // house line
        context.setColor(Color.black);
        context.drawRect(x1, y1, width, height);
    }

    public static class ElevatorActive implements Runnable {
        int index;

        ElevatorActive(int index) {
            this.index = index;
        }

        public void run() {
            while (true) {
                if (takeOrder()) {
                    while (go()) {
                        try {
                            Thread.sleep(MSpF);
                        } catch (InterruptedException e) {
                            System.out.println("Elevator has been interrupted: " + e.getMessage());
                            System.out.println(Thread.currentThread() + " finishes its work.");
                            break;
                        }
                    }
                }
            }
        }

        /**
         * Returns `true` if an order has been chosen successfully.
         */
        boolean takeOrder() {
            synchronized (queue) {
                if (queue.isEmpty()) return false;
                elevators[index].target = queue.removeFirst();
            }
            defineSpeed();
            return true;
        }

        /**
         * Changes an elevators Y coordinate, if a new floor achieved, tries to process people.
         * Returns `true` if still has a target, returns `false` if ready for a new order.
         */
        boolean go() {
            elevators[index].y += elevators[index].speed;
            animate();

            if (floorChanged()) {
                return processPeople();
            }
            return true;
        }

        /**
         * Processes people on the floor and inside an elevator.
         * Returns `true` if an elevator's target is completed, `false` if still has a target.
         */
        boolean processPeople() {
            int floor = elevators[index].floor;
            int speed = elevators[index].speed;
            boolean stop = false;

            synchronized (peopleOnFloors.get(floor)) {
                for (Person person : peopleOnFloors.get(floor)) {
                    if (elevators[index].fullness == elevators[index].size.number_of_people) break;

                    if (onOneWay(floor, person.destinationFloor, speed)) {
                        person.x = elevators[index].x;
                        elevators[index].peopleInElevator.add(person);
                        elevators[index].fullness++;
                        changeTarget(person.destinationFloor);
                        stop = true;
                    }
                }
                peopleOnFloors.get(floor).removeAll(elevators[index].peopleInElevator);
            }

            synchronized (disappearing) {
                for (Person person : elevators[index].peopleInElevator) {
                    if (person.destinationFloor == floor) {
                        disappearing.add(person);
                        stop = true;
                    }
                }
                elevators[index].peopleInElevator.removeAll(disappearing);
            }

            elevators[index].fullness = elevators[index].peopleInElevator.size();

            if (stop) try {
                Thread.sleep(400); // elevator stops to process people (visual)
            } catch (InterruptedException e) {
                System.out.println("Elevator was interrupted while a stop: " + e.getMessage());
            }

            synchronized (peopleOnFloors.get(floor)) {
                if (!peopleOnFloors.get(floor).isEmpty()) {
                    if (floor == elevators[index].target) {
                        elevators[index].speed = 0;
                        return true;
                    }
                    addOrder(floor);
                }
            }

            return floor != elevators[index].target;
        }

        /**
         * Checks if people can enter an elevator.
         */
        boolean onOneWay(int currentFloor, int destinationFloor, int speed) {
            return currentFloor < destinationFloor && speed < 0 || currentFloor > destinationFloor && speed > 0 || speed == 0;
        }

        /**
         * Checks if an elevator's Y coordinate matches any floor's height, updates floor.
         */
        boolean floorChanged() {
            elevators[index].floor = (y2 - elevators[index].y - 1) / floorHeight + 1;
            return (y2 - elevators[index].y) % floorHeight == 0;
        }

        /**
         * Updates elevator's target if possible.
         */
        void changeTarget(int destinationFloor) {
            if (elevators[index].speed == 0 || elevators[index].speed > 0 && elevators[index].target > destinationFloor || elevators[index].speed < 0 && elevators[index].target < destinationFloor) {
                elevators[index].target = destinationFloor;
                defineSpeed();
            }
        }

        /**
         * Sets elevator's speed depending on target.
         */
        void defineSpeed() {
            elevators[index].speed = Integer.compare(elevators[index].floor, elevators[index].target);
        }

        /**
         * Allows people in an elevator to move.
         */
        void animate() {
            for (Person person : elevators[index].peopleInElevator) {
                person.y = elevators[index].y + Elevator.height - Person.height;
                person.go(elevators[index].x, elevators[index].x + elevators[index].width);
            }
        }
    }

    static class Canvas extends JPanel {
        @Override
        public void paint(Graphics g) {
            super.paint(g);

            Skyscraper.paint(g, WINDOW_HEIGHT, WINDOW_WIDTH);
            synchronized (peopleOnFloors) {
                for (ArrayList<Person> floor : peopleOnFloors) {
                    for (int i = 0; i < floor.size(); i++) floor.get(i).paint(g);
                }
            }

            synchronized (disappearing) {
                for (Person person : disappearing) person.paint(g);
            }

            elevators[0].paint(g);
            elevators[1].paint(g);
        }
    }
}