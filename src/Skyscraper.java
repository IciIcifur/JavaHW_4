import javax.swing.*;
import java.awt.*;
import java.util.ArrayList;
import java.util.ConcurrentModificationException;
import java.util.NoSuchElementException;
import java.util.Random;

public class Skyscraper extends JFrame implements Runnable {
    // house params
    static int width = 400, height;
    static int floorHeight = 76, floorsNumber;
    static int x1, x2, y1, y2;
    static int attic = 24, elevatorsBoxWidth = 155, elevatorsBoxX1, elevatorsBoxX2;
    // people
    volatile static ArrayList<ArrayList<Person>> peopleOnFloors;
    volatile static ArrayList<Person> disappearing = new ArrayList<>();
    volatile static ArrayList<Integer> queue = new ArrayList<>();
    static int maximumPeopleOnFloor;
    // elevators
    volatile static Elevator[] elevators;
    // window
    static Canvas canvas;
    static int WINDOW_WIDTH;
    static int WINDOW_HEIGHT;
    // other
    static Random random = new Random();

    public Skyscraper(int w, int h, String title, int floorsNumber, int maximumPeopleOnFloor) {
        // canvas
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

        // house
        Skyscraper.floorsNumber = floorsNumber;
        height = floorHeight * floorsNumber + attic;
        Skyscraper.maximumPeopleOnFloor = maximumPeopleOnFloor;

        x1 = (WINDOW_WIDTH - Skyscraper.width) / 2;
        x2 = x1 + width;
        y2 = (WINDOW_HEIGHT - 10);
        y1 = y2 - height;

        // people
        peopleOnFloors = new ArrayList<>(floorsNumber);
        for (int i = 1; i <= floorsNumber + 1; i++) {
            peopleOnFloors.add(new ArrayList<>());
        }

        elevatorsBoxX1 = x1 + (width - elevatorsBoxWidth) / 2;
        elevatorsBoxX2 = x1 + (width - elevatorsBoxWidth) / 2 + elevatorsBoxWidth;

        // elevators
        elevators = new Elevator[2];
        elevators[0] = new Elevator(elevatorsBoxX1 + 10, y2 - Elevator.height, Elevator.Size.LARGE);
        elevators[1] = new Elevator(elevatorsBoxX2 - 10 - Elevator.Size.SMALL.width, y2 - Elevator.height, Elevator.Size.SMALL);
    }

    public void run() {
        Thread eSmall = new Thread(new ElevatorActive(0));
        Thread eLarge = new Thread(new ElevatorActive(1));

        eSmall.setPriority(4);
        eLarge.setPriority(4);

        eSmall.start();
        eLarge.start();

        while (true) {
            canvas.repaint();
            try {
                Thread.sleep(15);
                newPerson();
                animate();
            } catch (InterruptedException e) {
                System.out.println(e.getMessage());
            }
        }
    }

    synchronized void animate() {
        for (ArrayList<Person> floor : Skyscraper.peopleOnFloors) {
            for (Person person : floor)
                person.go(x1, x2);
        }

        ArrayList<Person> disappeared = new ArrayList<>();
        for (Person person : Skyscraper.disappearing) {
            if (person.disappear(person.destinationFloor == 1 ? 0 : x1, person.destinationFloor == 1 ? WINDOW_WIDTH : x2)) {
                disappeared.add(person);
            }
        }

        for (Person person : disappeared) Skyscraper.disappearing.remove(person);
    }

    void newPerson() {
        int currentFloor = 1 + random.nextInt(floorsNumber);
        int destinationFloor = 1 + random.nextInt(floorsNumber);
        if (peopleOnFloors.get(currentFloor).size() < maximumPeopleOnFloor && currentFloor != destinationFloor && random.nextInt(100) < 2) {
            int currentY = y1 + 1 + attic + (floorsNumber - currentFloor + 1) * floorHeight - Person.height;
            int currentX = x1 + 1 + random.nextInt(width - Person.width);
            peopleOnFloors.get(currentFloor).add(new Person(destinationFloor, currentX, currentY));
            addOrder(currentFloor);
        }
    }

    synchronized static void addOrder(int currentFloor) {
        for (int order : queue) {
            if (order == currentFloor) return;
        }
        queue.add(currentFloor);
    }

    static void paint(Graphics context, int windowH, int windowW) {
        // sky
        context.setColor(new Color(170, 205, 239));
        context.fillRect(0, 0, windowW, windowH);
        // grass
        context.setColor(new Color(151, 173, 101));
        context.fillRect(0, windowH - 20, windowW, 20);

        // house
        context.setColor(Color.white);
        context.fillRect(x1, y1, width, height);

        // elevators' box
        context.setColor(Color.lightGray);
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

        @Override
        public void run() {
            while (true) {
                if (takeOrder()) {
                    while (go()) {
                        try {
                            Thread.sleep(15);
                        } catch (InterruptedException e) {
                            System.out.println("Elevator has been interrupted.");
                        }
                    }
                }
            }
        }

        synchronized boolean takeOrder() {
            if (queue.isEmpty()) return false;

            try {
                elevators[index].target = queue.removeFirst();
            } catch (NoSuchElementException e) {
                System.out.println("Tried to take removed element.");
                return false;
            }

            defineSpeed();
            return true;
        }

        void defineSpeed() {
            elevators[index].speed = Integer.compare(elevators[index].floor, elevators[index].target);
        }

        synchronized boolean go() {
            elevators[index].y += Skyscraper.elevators[index].speed;
            animate();

            if (floorChanged()) {
                return processPeople();
            }
            return true;
        }

        synchronized boolean processPeople() {
            int floor = elevators[index].floor;
            int speed = Skyscraper.elevators[index].speed;
            boolean stop = false;

            for (Person person : Skyscraper.peopleOnFloors.get(floor)) {
                if (Skyscraper.elevators[index].fullness == Skyscraper.elevators[index].size.number_of_people) break;

                if (onOneWay(floor, person.destinationFloor, speed)) {
                    person.x = Skyscraper.elevators[index].x;
                    elevators[index].peopleInElevator.add(person);
                    elevators[index].fullness++;
                    changeTarget(person.destinationFloor);
                    stop = true;
                }
            }

            for (Person person : Skyscraper.elevators[index].peopleInElevator) {
                peopleOnFloors.get(floor).remove(person);
                if (person.destinationFloor == floor) Skyscraper.disappearing.add(person);
            }

            for (Person person : Skyscraper.disappearing) {
                if (Skyscraper.elevators[index].peopleInElevator.contains(person)) {
                    Skyscraper.elevators[index].peopleInElevator.remove(person);
                    Skyscraper.elevators[index].fullness--;
                    stop = true;
                }
            }

            if (stop) try {
                Thread.sleep(250);
            } catch (InterruptedException e) {
                System.out.println(e.getMessage());
            }

            if (!peopleOnFloors.get(floor).isEmpty()) {
                if (floor == elevators[index].target) {
                    elevators[index].speed = 0;
                    return true;
                }
                addOrder(floor);
            }

            return floor != elevators[index].target;
        }

        boolean floorChanged() {
            elevators[index].floor = (y2 - elevators[index].y - 1) / floorHeight + 1;
            return (y2 - elevators[index].y) % floorHeight == 0;
        }

        boolean onOneWay(int currentFloor, int destinationFloor, int speed) {
            return currentFloor < destinationFloor && speed < 0 || currentFloor > destinationFloor && speed > 0 || speed == 0;
        }

        void changeTarget(int destinationFloor) {
            if (elevators[index].speed == 0 || elevators[index].speed > 0 && elevators[index].target > destinationFloor || elevators[index].speed < 0 && elevators[index].target < destinationFloor) {
                elevators[index].target = destinationFloor;
                defineSpeed();
            }
        }

        void animate() {
            for (Person person : elevators[index].peopleInElevator) {
                person.y = elevators[index].y + Elevator.height - Person.height;
                person.go(elevators[index].x, elevators[index].x + elevators[index].width);
            }
        }
    }

    static class Canvas extends JPanel {
        @Override
        synchronized public void paint(Graphics g) {
            super.paint(g);

            Skyscraper.paint(g, WINDOW_HEIGHT, WINDOW_WIDTH);

            for (ArrayList<Person> floor : Skyscraper.peopleOnFloors) {
                try {
                    for (Person person : floor) person.paint(g);
                } catch (ConcurrentModificationException e) {
                    System.out.println("Error while drawing people on the floor.");
                }
            }

            try {
                for (Person person : Skyscraper.disappearing) person.paint(g);
            } catch (ConcurrentModificationException e) {
                System.out.println("Error while drawing disappearing people.");
            }
            try {
                for (Elevator e : Skyscraper.elevators) e.paint(g);
            } catch (ConcurrentModificationException e) {
                System.out.println("Error while drawing elevators.");
            }

        }
    }
}