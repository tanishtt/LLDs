package com.tanish.lld.composite;


import java.util.ArrayList;
import java.util.List;

abstract class FileSystemNode{
    protected String name;
    protected Folder parent;

    protected FileSystemNode(String name){
        this.name=name;
    }

    public String getName() {
        return name;
    }

    public void setParent(Folder parent){
        this.parent=parent;
    }
    public Folder getParent(){
        return parent;
    }

    public abstract long getSize();
    public abstract void printAll(String indent);
    public abstract int countFiles();
    public abstract FileSystemNode deepCopy();
//    public abstract void ls();
//    public abstract FileSystemNode cd(String name);

}

class Folder extends FileSystemNode{

    private List<FileSystemNode> children=new ArrayList<>();

    protected Folder(String name) {
        super(name);
    }

    public void addChild(FileSystemNode child){
        child.setParent(this);
        children.add(child);
    }

    public void removeChild(FileSystemNode child){
        child.setParent(null);
        children.remove(child);
    }

    public FileSystemNode getChild(String name){
        for (FileSystemNode child : children){
            if (child.getName().equals(name)){
                return child;
            }
        }

        return null;
    }

    public List<FileSystemNode> getChildren(){
        return children;
    }

    @Override
    public long getSize() {
        int total=0;
        for(FileSystemNode child: children){
            total+=child.getSize();
        }
        return total;
    }

    @Override
    public void printAll(String indent) {
        System.out.println(indent+"+ "+name);
        for (FileSystemNode child: children){
            child.printAll(indent+"  ");
        }
    }

    @Override
    public int countFiles() {
        int total=0;
        for(FileSystemNode child: children){
            total+=child.countFiles();
        }
        return total;
    }

    @Override
    public FileSystemNode deepCopy() {
        Folder copy=new Folder(name);
        for (FileSystemNode child : children){
            copy.addChild(child.deepCopy());
        }
        return copy;
    }
}

class File extends FileSystemNode{
    private long size;
    protected File(String name, long size) {
        super(name);
        this.size=size;
    }

    @Override
    public long getSize() {
        return size;
    }

    @Override
    public void printAll(String indent) {
        System.out.println(indent+"- "+this.name);
    }

    @Override
    public int countFiles() {
        return 1;
    }

    @Override
    public FileSystemNode deepCopy() {
        return new File(name,size);
    }
}

class FileSystemService{
    private Folder root=new Folder("root");
    public Folder getRoot(){
        return root;
    }

    //FIND NODE
    public FileSystemNode findNode(String path){
        if(path.isEmpty() || path.equals("/")){
            return root;
        }

        String parts[]=path.split("/");
        FileSystemNode current=root;

        for (int i=0; i<parts.length; i++){
            if(!(current instanceof Folder)){
                return null;
            }
            current=((Folder)current).getChild(parts[i]);
        }

        return current;
    }
    //CREATE FOLDER
    public void createFolder(String path){
        String parts[]=path.split("/");
        Folder current=root;
        for (int i=0; i<parts.length; i++){
            FileSystemNode child=((Folder)current).getChild(parts[i]);
            if(child == null){
                Folder newFolder=new Folder(parts[i]);
                current.addChild(newFolder);
                current=newFolder;
            }else{
                if(child instanceof File){
                    throw new RuntimeException("File exists with path name.");
                }else{
                    current= (Folder) child;
                }
            }
        }
    }
    //CREATE FILE
    public void createFile(String pathAndFileName, long size){
        int lastSlashIndex=pathAndFileName.lastIndexOf('/');
        String fileName=pathAndFileName.substring(lastSlashIndex+1);
        String folderName=pathAndFileName.substring(0, lastSlashIndex);
        createFolder(folderName);
        Folder parent= (Folder) findNode(folderName);

        File newFile=new File(fileName, size);
        parent.addChild(newFile);

    }
    //DELETE
    public void delete(String path){
        FileSystemNode node=findNode(path);
        if(node == null || node == root){
            return;
        }

        node.getParent().removeChild(node);
    }
    //SEARCH
    public List<String> search(String fileName){
        List<String> result=new ArrayList<>();
        searchDFS(root, "", fileName,result);
        return result;
    }
    private void searchDFS(FileSystemNode node, String currentPath, String targetName, List<String>result){
        String path=currentPath+"/"+node.getName();
        if(node.getName().equals(targetName)){
            result.add(path);
        }

        if(node instanceof Folder){
            for (FileSystemNode child: ((Folder) node).getChildren()){
                searchDFS(child, path, targetName, result);
            }
        }
    }
    //MOVE
    public void move(String sourcePath, String destinationPath){
        Folder destination= (Folder) findNode(destinationPath);
        FileSystemNode source= findNode(sourcePath);
        source.getParent().removeChild(source);
        destination.addChild(source);
    }
    //COPY
    public void copy(String sourcePath, String destinationPath){
        Folder destination= (Folder) findNode(destinationPath);
        FileSystemNode source= findNode(sourcePath);
        destination.addChild(source.deepCopy());
    }
    //PRINT
    public void printAll(){
        root.printAll("");
    }
    //SIZE
    public long getSize(String path){
        FileSystemNode node=findNode(path);
        return node.getSize();
    }
    //COUNT FILES
    public long countFiles(String path){
        FileSystemNode node=findNode(path);
        return node.countFiles();
    }
}

public class FileSystemDriver {
//    public static void main(String[] args) {
//        Folder root=new Folder("root");
//        Folder documents=new Folder("documents");
//        File f1=new File("resume.pdf",2);
//        File f2=new File("notes.pdf",3);
//        documents.addChild(f1);
//        documents.addChild(f2);
//
//        Folder photos=new Folder("photos");
//        File f3=new File("img1.jpg",5);
//        File f4=new File("img2.jpg",3);
//        photos.addChild(f3);
//        photos.addChild(f4);
//
//        File f5=new File("movie.mp4",6);
//
//        root.addChild(documents);
//        root.addChild(photos);
//        root.addChild(f5);
//        System.out.println("Size of root: "+root.getSize());
//
//        root.printAll("");
//    }

    public static void main(String[] args) {
        FileSystemService fs=new FileSystemService();
        fs.createFolder("documents");
        fs.createFolder("images");
        fs.createFolder("documents/imp");

        fs.createFile("documents/resume.pdf", 200);
        fs.createFile("documents/notes.txt", 100);
        fs.createFile("documents/imp/notes.txt", 50);


        fs.createFolder("photos/vacation");
        fs.createFolder("photos/imp");
        fs.createFile("photos/vacation/img1.jpeg",50);
        fs.createFile("photos/vacation/img2.jpeg",30);
        fs.createFile("photos/imp/notes.jpeg",10);

        fs.printAll();

        System.out.println("Size of /documents : "+fs.getSize("documents"));
        System.out.println("Count files of /photos : "+fs.countFiles("photos"));
        System.out.println("Search notes : "+fs.search("notes.txt"));
        fs.move("documents/resume.pdf", "photos");
        fs.copy("photos/vacation", "documents");
        fs.printAll();
    }
}
