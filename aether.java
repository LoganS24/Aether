import java.util.Scanner;

//Create the JShell / RPL through a class demonstration

public class aether {
    public static void main(String[] args) {
        if(args.length == 0) {
            System.out.println("No arguments detected, entering AetherTest mode...");
            //AetherTest Mode:
            runJShell();
        }
        else if(args.length == 1){
            System.out.println("Argument detected: " + args[0]);
            // Handle the argument as needed
            System.out.println("Scanner not yet implemented.");
        }
        else{
            System.out.println("Scanner not yet implemented.");
        }
    }

    public static void runJShell() {
        // Implementation for JShell mode
        while(true) {
            System.out.print("AetherTest> ");
            // Read user input and evaluate commands
            // For demonstration, we will just read input and print it back
            Scanner scan = new Scanner(System.in);
            String input = scan.nextLine();
            if(input.equalsIgnoreCase("exit")) {
                System.out.println("Exiting AetherTest mode...");
                break;
            }
            System.out.println(input + "\nScanner not implemented yet...");
        }
    }
    
}


