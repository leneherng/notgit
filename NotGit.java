package notgit;

import java.io.*;
import java.util.*;
import java.nio.file.Path;
import java.nio.file.Paths;
import notgit.Tree.TreeNode;

public class NotGit {

    private static Tree<String> tree = new Tree();
    private static boolean notgit = false;

    public static void notgit() {
        System.out.println("Type not-git to begin!");
        Scanner sc = new Scanner(System.in);
        String s = sc.next();
        if (s.equalsIgnoreCase("not-git")) {
            notgit = true;
            System.out.println("\nWelcome to not-git!");
            System.out.println("First, type init to initialise repository");
            System.out.println("then:");
            System.out.println("add      to track a file");
            System.out.println("branch   to create a branch or list all branches");
            System.out.println("checkout to change branches");
            System.out.println("commit   to commit repository");
            System.out.println("revert   to revert to a previous commit");
            System.out.println("status   to see the list of files");
            System.out.println("end      to close not-git\n");
            ObjectInputStream in;
            try {
                in = new ObjectInputStream(new FileInputStream("tree.dat"));
                tree = (Tree) in.readObject();
            } catch (FileNotFoundException ex) {
            } catch (IOException | ClassNotFoundException ex) {
            }
        } else if (s.equalsIgnoreCase("end")) {
            end();
        } else {
            System.out.println("No such command. Please check again.\n");
            notgit();
        }
    }

    public static void scan() {
        Scanner sc = new Scanner(System.in);
        String s = sc.nextLine();
        if (s.startsWith("end")) {
            end();
        } else if (s.startsWith("init")) {
            if (tree.directory != null) { //if directory is present
                System.out.println("Repository is already initialised.\n");
            } else {
                System.out.println("Directory: [type '.' to use the current folder]");
                Scanner scan = new Scanner(System.in);
                boolean legitDirectory = false;
                while (legitDirectory == false) {
                    tree.directory = scan.nextLine();
                    if (tree.directory.equals(".")) {
                        tree.directory = "";
                        init(".");
                        break;
                    } else if (tree.directory.contains("\\")) {
                        tree.directory += "\\";
                        init(tree.directory);
                        break;
                    } else {
                        System.out.println("please recheck the directory\n");
                    }
                }
            }
        } else if (s.startsWith("status")) {
            status();
        } else if (s.startsWith("add")) {
            s = s.substring(3, s.length());
            add(s);
        } else if (s.startsWith("branch")) {
            s = s.substring(6);
            if (s.isEmpty()) {
                branch();
            } else {
                s = s.substring(1);
                branch(s);
            }
        } else if (s.startsWith("commit")) {
            commit();
        } else if (s.startsWith("checkout")) {
            s = s.substring(9);
            checkout(s);
        } else if (s.startsWith("diff")) {
            s = s.substring(5);
            diff(s);
        } else if (s.startsWith("log")) {
            log();
        } else if (s.startsWith("tree")) {
            tree();
        } else if (s.startsWith("revert")) {
            int i = Integer.parseInt(s.substring(7, s.length()));
            revert(i);
        } else {
            System.out.println("No such command. Please check again.\n");
        }
    }

    // initialise folder as a repository
    public static void init(String directory) {
        File repo = new File(directory);
        // add every filename in the repository into an ArrayList
        for (File f : repo.listFiles()) {
            tree.repoList.add(f.getName());
        }
        System.out.println();
    }

    // add a file to be tracked
    public static void add(String fileName) {
        if (tree.directory == null) { //no directory = no repository
            System.out.println("Repository is not initialised.\n");
        } else {
            fileName = fileName.substring(1, fileName.length());
            if (!tree.repoList.contains(fileName)) { //if the repository does not contain the file
                System.out.println(fileName + " is not in the repository.\n");
            } else {
                tree.newList.add(fileName);
                tree.repoList.remove(fileName);
                System.out.println(fileName + " is now being tracked.\n");
            }
        }
    }

    // list out all branches
    public static void branch() {
        if (tree.isEmpty()) {  
            System.out.println("No branch available.\n");
        } else {
            tree.branchList();
        }
    }

    // create new branch
    public static void branch(String branchName) {
        if (tree.isEmpty()) {
            System.out.println("Nothing to branch from.\n");
        } else {
            if (tree.branchList.contains(branchName)) {
                System.out.println("This name is already used.\n");
            } else {
                tree.addBranch(branchName);
                System.out.println("\"" + branchName + "\" created.\n");
            }
        }
    }

    // checks out the other branch
    public static void checkout(String branchName) {
        boolean unsavedChanges = false;
        if (!tree.branchList.contains(branchName)) {
            System.out.println("This branch does not exist.\n");
        } else {
            try {
                for (String s : tree.newList) {
                    //scan file
                    Scanner sc = new Scanner(new FileInputStream(s));
                    String st = "";
                    while (sc.hasNextLine()) {
                        st += sc.nextLine() + System.lineSeparator();
                    }
                    if (!st.equals(tree.leaf.getBlob(s))) { // if current file does not equal to file committed
                        unsavedChanges = true;  // then there are unsaved changes
                        break;
                    }
                }
                for (String s : tree.modifyList) {
                    //scan file
                    Scanner sc = new Scanner(new FileInputStream(s));
                    String st = "";
                    while (sc.hasNextLine()) {
                        st += sc.nextLine() + System.lineSeparator();
                    }
                    if (!st.equals(tree.leaf.getBlob(s))) { // if current file equals to file committed
                        unsavedChanges = true;  // then there are unsaved changes
                        break;
                    }
                }
                for (String s : tree.trackList) {
                    //scan file
                    Scanner sc = new Scanner(new FileInputStream(s));
                    String st = "";
                    while (sc.hasNextLine()) {
                        st += sc.nextLine() + System.lineSeparator();
                    }
                    if (!st.equals(tree.leaf.getBlob(s))) { // if current file equals to file committed
                        unsavedChanges = true;  // then there are unsaved changes
                        break;
                    }
                }
                if (unsavedChanges == false) {  //if there are no unsaved changes
                    tree.checkout(branchName);  
                    System.out.println("checkout to branch \"" + branchName + "\"\n");
                    for (int j = 0; j < tree.newList.size(); j++) {
                        try {
                            File file = new File(tree.directory + "" + tree.newList.get(j));
                            PrintWriter pr = new PrintWriter(file);
                            pr.println(tree.leaf.getBlob(tree.newList.get(j)));
                            pr.close();
                        } catch (FileNotFoundException e) {
                            System.out.println("Problem with file output.");
                        }
                    }
                    for (int j = 0; j < tree.modifyList.size(); j++) {
                        try {
                            File file = new File(tree.directory + "" + tree.modifyList.get(j));
                            PrintWriter pr = new PrintWriter(file);
                            pr.println(tree.leaf.getBlob(tree.modifyList.get(j)));
                            pr.close();
                        } catch (FileNotFoundException e) {
                            System.out.println("Problem with file output.");
                        }
                    }
                    for (int j = 0; j < tree.trackList.size(); j++) {
                        try {
                            File file = new File(tree.directory + "" + tree.trackList.get(j));
                            PrintWriter pr = new PrintWriter(file);
                            pr.println(tree.leaf.getBlob(tree.trackList.get(j)));
                            pr.close();
                        } catch (FileNotFoundException e) {
                            System.out.println("Problem with file output.");
                        }
                    }
                } else {    // if there are unsaved changes
                    System.out.println("There are modified files! Please commit or remove the changes before switching to another branch.\n");
                }
            } catch (IOException ex) {
                System.out.println("File maybe deleted.\n");
            }
        }
    }

    public static void commit() {
        if (tree.trackList.isEmpty() && tree.newList.isEmpty() && tree.modifyList.isEmpty()) {
            System.out.println("Nothing to commit.\n");
        } else {
            System.out.print("description: ");
            Scanner sc = new Scanner(System.in);
            String description = sc.nextLine();
            tree.addTreeNode(description);
            tree.treelog();
            //for every file in the tracked list
            for (int i = 0; i < tree.trackList.size(); i++) {
                Path path = Paths.get(tree.directory + tree.trackList.get(i));
                String s = "";
                try {
                    Scanner in = new Scanner(path);
                    while (in.hasNextLine()) {
                        s += in.nextLine() + System.lineSeparator();
                    }
                    tree.leaf.addBlob(tree.trackList.get(i), s);
                    in.close();
                } catch (IOException e) {
                    System.out.println("Input error\n");
                }
            }
            for (int i = 0; i < tree.newList.size(); i++) {
                Path path = Paths.get(tree.directory + tree.newList.get(i));
                String s = "";
                try {
                    Scanner in = new Scanner(path);
                    while (in.hasNextLine()) {
                        s += in.nextLine() + System.lineSeparator();
                    }
                    tree.leaf.addBlob(tree.newList.get(i), s);
                    in.close();
                } catch (IOException e) {
                    System.out.println("Input error\n");
                }
            }
            for (int i = 0; i < tree.modifyList.size(); i++) {
                Path path = Paths.get(tree.directory + tree.modifyList.get(i));
                String s = "";
                try {
                    Scanner in = new Scanner(path);
                    while (in.hasNextLine()) {
                        s += in.nextLine() + System.lineSeparator();
                    }
                    tree.leaf.addBlob(tree.modifyList.get(i), s);
                    in.close();
                } catch (IOException e) {
                    System.out.println("Input error\n");
                }
            }
            System.out.printf("files committed with commit-id \"%d\"\n", tree.leaf.getCommitID());
            System.out.println("type log to see commit log");
            System.out.println("type tree to see commit tree\n");
        }
    }

    public static void diff(String fileName) {
        if (tree.directory == null) { //no directory = no repository
            System.out.println("Repository is not initialised.\n");
        } else {
            try {
                if (tree.leaf == null) { // if there is leaf in tree
                    System.out.println("Nothing to compare to.\n");
                } else {
                    Scanner blobscan = new Scanner(tree.leaf.getBlob(fileName));
                    ArrayList<String> blobString = new ArrayList<>();
                    while (blobscan.hasNextLine()) {
                        String b = blobscan.nextLine();
                        blobString.add(b);
                    }
                    Scanner filescan = new Scanner(new FileInputStream(fileName));
                    ArrayList<String> fileString = new ArrayList<>();
                    while (filescan.hasNextLine()) {
                        String f = filescan.nextLine();
                        fileString.add(f);
                    }
                    int i = 0;
                    while (i < blobString.size()) {
                        int j = 0;
                        while (j < fileString.size()) {
                            if (blobString.get(i).equals(fileString.get(j))) {
                                blobString.remove(i);
                                fileString.remove(j);
                                break;
                            } else {
                                j++;
                            }
                        }
                        if (j == fileString.size()) {
                            i++;
                        }
                    }
                    if (fileString.isEmpty() && blobString.isEmpty()) {
                        System.out.println("There is no difference.\n");
                    } else {
                        for (i = 0; i < fileString.size(); i++) {
                            System.out.println(" + " + fileString.get(i));
                        }
                        for (i = 0; i < blobString.size(); i++) {
                            System.out.println(" - " + blobString.get(i));
                        }
                    }
                    System.out.println();
                }
            } catch (IOException e) {
                System.out.println("Problem with file input.\n");
            }
        }
    }

    public static void log() {
        tree.log();
        System.out.println();
    }

    public static void revert(int commitID) {
        //if there is no tree
        if (tree.isEmpty()) {
            System.out.println("Nothing to revert to.\n");
        } else if (commitID > tree.commitID) {
            System.out.printf("Commit ID \"%d\" does not exist.\n\n", commitID);
        } else {
            TreeNode current = tree.leaf;
            for (int i = 0; i < tree.leaf.commitID; i++) {
                if (current.getCommitID() == commitID) {
                    System.out.print("description: ");
                    Scanner sc = new Scanner(System.in);
                    String description = sc.nextLine();
                    tree.addTreeNode(description);
                    tree.treelog();
                    tree.leaf.blobs = (HashMap) current.blobs.clone();
                    //print the files out 
                    for (int j = 0; j < tree.newList.size(); j++) {
                        try {
                            File file = new File(tree.directory + "" + tree.newList.get(j));
                            PrintWriter pr = new PrintWriter(file);
                            pr.println(tree.leaf.getBlob(tree.newList.get(j)));
                            pr.close();
                        } catch (FileNotFoundException e) {
                            System.out.println("Problem with file output.");
                        }
                    }
                    for (int j = 0; j < tree.modifyList.size(); j++) {
                        try {
                            File file = new File(tree.directory + "" + tree.modifyList.get(j));
                            PrintWriter pr = new PrintWriter(file);
                            pr.println(tree.leaf.getBlob(tree.modifyList.get(j)));
                            pr.close();
                        } catch (FileNotFoundException e) {
                            System.out.println("Problem with file output.");
                        }
                    }
                    for (int j = 0; j < tree.trackList.size(); j++) {
                        try {
                            File file = new File(tree.directory + "" + tree.trackList.get(j));
                            PrintWriter pr = new PrintWriter(file);
                            pr.println(tree.leaf.getBlob(tree.trackList.get(j)));
                            pr.close();
                        } catch (FileNotFoundException e) {
                            System.out.println("Problem with file output.");
                        }
                    }
                    System.out.printf("revert to commit-id \"%d\" and committed with commit-id \"%d\"\n", commitID, tree.leaf.getCommitID());
                    System.out.println("type log to see commit log");
                    System.out.println("type tree to see commit tree\n");
                    break;
                }
                if (current == tree.root && commitID <= tree.commitID) {
                    System.out.println("This commit is not from this branch.\n");
                    break;
                } else {
                    current = current.trunk;
                }
            }
        }
    }

    public static void status() {
        if (tree.directory == null) {
            System.out.println("No repository initialised.\n");
        } else {
            //scan the repository
            while (!tree.repoList.isEmpty()) {
                tree.repoList.remove(0);
            }
            if (tree.directory.equals("")) {
                init(".");
            } else if (tree.directory.contains("\\")) {
                tree.directory += "\\";
                init(tree.directory);
            }
            //remove tracked files from the list
            for (String s : tree.newList) {
                if (tree.repoList.contains(s)) {
                    tree.repoList.remove(s);
                }
            }
            for (String s : tree.modifyList) {
                if (tree.repoList.contains(s)) {
                    tree.repoList.remove(s);
                }
            }
            for (String s : tree.trackList) {
                if (tree.repoList.contains(s)) {
                    tree.repoList.remove(s);
                }
            }
            //if these lists are empty, no files are tracked
            if (tree.trackList.isEmpty() && tree.newList.isEmpty() && tree.modifyList.isEmpty()) {
                System.out.println("No file is being tracked.");
            } else {
                try {
                    for (String s : tree.newList) {
                        //scan file
                        Scanner sc = new Scanner(new FileInputStream(s));
                        String st = "";
                        while (sc.hasNextLine()) {
                            st += sc.nextLine() + System.lineSeparator();
                        }
                        if (tree.leaf != null) { // if there was a commit
                            if (tree.leaf.contains(s)) { //if the commit contained the file
                                if (!st.equals(tree.leaf.getBlob(s))) { // if current file does not equal to file committed
                                    tree.modifyList.add(s);
                                }
                            }
                        }
                    }
                for (String s : tree.modifyList) {
                    if (tree.newList.contains(s)) {
                        tree.newList.remove(s);
                    }
                }
                    for (String s : tree.modifyList) {
                        //scan file
                        Scanner sc = new Scanner(new FileInputStream(s));
                        String st = "";
                        while (sc.hasNextLine()) {
                            st += sc.nextLine() + System.lineSeparator();
                        }
                        if (tree.leaf != null) { // if there was a commit
                            if (tree.leaf.contains(s)) { //if the commit contained the file
                                if (st.equals(tree.leaf.getBlob(s))) { // if current file equals to file committed
                                    tree.trackList.add(s);
                                }
                            }
                        }
                    }
                for (String s : tree.trackList) {
                    if (tree.modifyList.contains(s)) {
                        tree.modifyList.remove(s);
                    }
                }
                    for (String s : tree.trackList) {
                        //scan file
                        Scanner sc = new Scanner(new FileInputStream(s));
                        String st = "";
                        while (sc.hasNextLine()) {
                            st += sc.nextLine() + System.lineSeparator();
                        }
                        if (tree.leaf != null) { // if there was a commit
                            if (tree.leaf.contains(s)) { //if the commit contained the file
                                if (!st.equals(tree.leaf.getBlob(s))) { // if current file does not equal to file committed
                                    tree.modifyList.add(s);
                                }
                            }
                        }
                    }
                for (String s : tree.modifyList) {
                    if (tree.trackList.contains(s)) {
                        tree.trackList.remove(s);
                    }
                }
                } catch (IOException ex) {
                    System.out.println("File maybe deleted.\n");
                }

                //print new files, modified files, tracked files
                if (!tree.newList.isEmpty()) {
                    System.out.println("new files: ");
                    for (String s : tree.newList) {
                        System.out.println(s);
                    }
                }
                if (!tree.modifyList.isEmpty()) {
                    System.out.println("\nmodified files: ");
                    for (String s : tree.modifyList) {
                        System.out.println(s);
                    }
                }
                if (!tree.trackList.isEmpty()) {
                    System.out.println("\ntracked files: ");
                    for (String s : tree.trackList) {
                        if (tree.newList.contains(s)) {
                            tree.newList.remove(s);
                        }
                        System.out.println(s);
                    }
                }
            }
            //print untracked files
            if (!tree.repoList.isEmpty()) {
                System.out.println("\nuntracked files:");
                for (String s : tree.repoList) {
                    System.out.println(s);
                }
                System.out.println();
            }
        }
    }

    //displays a tree of commits
    public static void tree() {
        tree.tree();
    }

    //ends the program
    public static void end() {
        notgit = false;
        ObjectOutputStream out;
        try {
            out = new ObjectOutputStream(new FileOutputStream("tree.dat"));
            out.writeObject(tree);
        } catch (FileNotFoundException ex) {
        } catch (IOException ex) {
        }
    }

    public static void main(String[] args) {
        notgit();
        while (notgit == true) {
            scan();
        }
    }
}
