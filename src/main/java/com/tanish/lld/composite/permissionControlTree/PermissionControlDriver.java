package com.tanish.lld.composite.permissionControlTree;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

//acts as component, composite, leaf
class Resource{
    private final String name;
    private Resource parent;
    private final List<Resource> children;
    private final List<PermissionAssignment> assignments;

    Resource(String name) {
        this.name = name;
        this.children = new ArrayList<>();
        this.assignments = new ArrayList<>();
    }

    public void addChild(Resource child){
        children.add(child);
        child.parent = this;
    }
    public void removeChild(Resource child){
        children.remove(child);
        child.parent = null;
    }

    public void assignPermission(User user, Permission permission, PermissionEffect permissionEffect){
        assignments.add(new PermissionAssignment(user, permissionEffect, permission));
    }

    public String getName() {
        return name;
    }

    public Resource getParent() {
        return parent;
    }

    public List<Resource> getChildren() {
        return children;
    }

    public List<PermissionAssignment> getAssignments() {
        return assignments;
    }
}

enum Permission{
    READ,
    WRITE,
    DELETE,
    ADMIN
}
enum PermissionEffect{
    ALLOW,
    DENY
}
class User{
    private final String id;
    private final String name;

    User(String name) {
        this.id = UUID.randomUUID().toString();
        this.name = name;
    }

    public String getId() {
        return id;
    }

    public String getName() {
        return name;
    }
}

class PermissionAssignment{
    private final User user;
    private final PermissionEffect permissionEffect;
    private final Permission permission;

    PermissionAssignment(User user, PermissionEffect permissionEffect, Permission permission) {
        this.user = user;
        this.permissionEffect = permissionEffect;
        this.permission = permission;
    }

    public User getUser() {
        return user;
    }

    public PermissionEffect getPermissionEffect() {
        return permissionEffect;
    }

    public Permission getPermission() {
        return permission;
    }
}

class PermissionService{
    public boolean hasPermission(User user, Resource resource, Permission permission){
        Resource current=resource;
        while (current!=null){
            for(PermissionAssignment assignment : current.getAssignments()){
                if (assignment.getUser().getId().equals(user.getId()) && assignment.getPermission() == permission){
                    return assignment.getPermissionEffect() == PermissionEffect.ALLOW;
                }
            }
            current=current.getParent();
        }

        return false;
    }
}

class TreePrinter {
    public static void print(Resource resource) {
        print(resource, "");
    }

    private static void print(Resource resource, String indent) {
        System.out.println(indent + resource.getName());
        for (Resource child : resource.getChildren()) {
            print(child, indent + "  ");
        }
    }
}


public class PermissionControlDriver {
    public static void main(String[] args) {
        PermissionService ps= new PermissionService();

        User tanish= new User("tanish");

        Resource Organization=new Resource("Organization");
        Resource Engineering=new Resource("Engineering");
        Resource BackendTeam=new Resource("Backend Team");
        Resource ServiceA=new Resource("Service A");
        Resource ServiceB=new Resource("Service B");
        Resource FrontendTeam=new Resource("Frontend Team");

        Organization.addChild(Engineering);

        Engineering.addChild(BackendTeam);
        Engineering.addChild(FrontendTeam);

        BackendTeam.addChild(ServiceA);
        BackendTeam.addChild(ServiceB);

        //Engineering READ ALLOW
        Engineering.assignPermission(
                tanish,
                Permission.READ,
                PermissionEffect.ALLOW
        );

        //ServiceA READ DENY
        ServiceA.assignPermission(
                tanish,
                Permission.READ,
                PermissionEffect.DENY
        );

        System.out.println(ps.hasPermission(tanish,BackendTeam, Permission.READ));
        System.out.println(ps.hasPermission(tanish, ServiceB, Permission.READ));
        System.out.println(ps.hasPermission(tanish, ServiceA, Permission.READ));
        System.out.println();

        TreePrinter.print(Organization);


    }
}
