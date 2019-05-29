package notgit;

import java.io.*;
import java.util.*;
import java.text.SimpleDateFormat;

public class Tree<E> implements Serializable {

    String directory;
    ArrayList<String> repoList = new ArrayList<>();
    ArrayList<String> trackList = new ArrayList<>();
    ArrayList<String> modifyList = new ArrayList<>();
    ArrayList<String> newList = new ArrayList<>();
    ArrayList<String> branchList = new ArrayList<>();
    ArrayList<String> log = new ArrayList<>();
    ArrayList<String> treelog = new ArrayList<>();

    int commitID = 0;
    TreeNode<E> root;
    TreeNode<E> leaf;
    String branchName = "master";
    HashMap<String, TreeNode<E>> head = new HashMap<>();

    public Tree() {
    }

    public void addTreeNode(String description) {
        if (leaf == null) {
            root = leaf = new TreeNode<>(description, ++commitID);
            head.put(branchName, leaf);
            branchList.add(branchName);
            log.add("commit \"" + leaf.commitID + "\" - " + leaf.getDate() + "\n       \"" + leaf.description + "\"");
        } else {
            TreeNode<E> newNode = new TreeNode<>(description, ++commitID);
            leaf.branch.add(newNode);
            newNode.trunk = leaf;
            leaf = newNode;
            log.add("commit \"" + leaf.commitID + "\" - " + leaf.getDate() + "\n       \"" + leaf.description + "\"");
            head.replace(branchName, leaf);
        }
    }

    public void addBranch(String branchName) {
        this.branchName = branchName;
        branchList.add(branchName);
        TreeNode<E> branchLeaf = leaf;
        head.put(branchName, branchLeaf);
        String s = "";
        for (String st : branchList) {
            if (!branchName.equals(st)) {
                s += " |";
            } else {
                s += "\\ ";
            }
        }
        treelog.add(s);
    }

    public void checkout(String branchName) {
        this.branchName = branchName;
        leaf = head.get(branchName);
    }

    public void log() {
        if (log.isEmpty()) {
            System.out.println("Log is empty.");
        } else {
            for (String s : log) {
                System.out.println(s);
            }
        }
    }

    public void tree() {
        if (treelog.isEmpty()) {
            System.out.println("Log is empty.");
        } else {
            for (String s : treelog) {
                System.out.println(s);
            }
        }
        System.out.println();
    }

    public void treelog() {
        String s = "";
        if (branchList.size() > 1) {
            for (String st : branchList) {
                if (branchName.equals(st)) {
                    s += " *";
                } else {
                    s += " |";
                }
            }
            s += " " + leaf.commitID + " : " + leaf.description;
            treelog.add(s);
        } else {
            treelog.add(" * " + leaf.commitID + ": " + leaf.description);
        }
    }

    public void branchList() {
        for (String s : branchList) {
            if (branchName.equals(s)) {
                System.out.print("*");
            }
            System.out.println(s);
        }
        System.out.println();
    }

    @Override
    public String toString() {
        return "";
    }

    public boolean isEmpty() {
        return root == null;
    }

    public TreeNode<E> getRoot() {
        return root;
    }

    public static class TreeNode<E> implements Serializable {

        int commitID;
        String description;
        Date date;
        TreeNode<E> trunk;
        HashMap<String, String> blobs = new HashMap<>();
        ArrayList<TreeNode<E>> branch = new ArrayList<>();

        public TreeNode() {
        }

        public TreeNode(String description, int commitID) {
            this.description = description;
            this.commitID = commitID;
            this.date = new Date();
        }

        public void addBlob(String fileName, String element) {
            this.blobs.put(fileName, element);
        }

        public String getBlob(String fileName) {
            return blobs.get(fileName);
        }

        public HashMap<String, String> getBlobs() {
            return blobs;
        }

        public void setBlobs(HashMap<String, String> blobs) {
            this.blobs = blobs;
        }

        public TreeNode<E> getTrunk() {
            return trunk;
        }

        public boolean contains(String fileName) {
            if (blobs.containsKey(fileName)) {
                return true;
            } else {
                return false;
            }
        }

        public int getCommitID() {
            return this.commitID;
        }

        public String getDescription() {
            return description;
        }

        public String getDate() {
            SimpleDateFormat sdf = new SimpleDateFormat("HH:mm dd-MM-yyyy");
            return sdf.format(date);
        }

        @Override
        public String toString() {
            String s = getCommitID() + System.lineSeparator();
            s += getDescription() + System.lineSeparator();
//            s += getDate() + System.lineSeparator();
//            s += getTrunk() + System.lineSeparator();
//            s += getBlobs();
            return s;
        }
    }
}
