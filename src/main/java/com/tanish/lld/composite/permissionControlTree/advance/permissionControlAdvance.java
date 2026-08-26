package com.tanish.lld.composite.permissionControlTree.advance;

import java.util.*;

/*
* "I'll model the organization as a resource tree.
* Each resource can have permission assignments for either users or groups.
* A user can belong to multiple groups. Permission checks start at the requested resource
* and walk toward the root. The most specific assignment wins; within the same resource,
* DENY takes precedence over ALLOW. If no assignment is found, access is denied by default."
* */
enum Permission {
    READ,
    WRITE,
    DELETE,
    ADMIN
}

enum PermissionEffect {
    ALLOW,
    DENY
}
/* ============================================================
                        USER & GROUP
   ============================================================ */

class User{
    private final String id;
    private final String name;
    private final Set<Group> groups = new HashSet<>();

    User(String id, String name) {
        this.id = id;
        this.name = name;
    }

    public String getId() {
        return id;
    }

    public String getName() {
        return name;
    }

    public Set<Group> getGroups() {
        return Collections.unmodifiableSet(groups);
    }

    void addGroup(Group group){
        groups.add(group);
    }
    void removeGroup(Group group){
        groups.remove(group);
    }

    @Override
    public String toString() {
        return name + "(" + id + ")";
    }
}

class Group{
    private final String id;
    private final String name;
    private final Set<User> users=new HashSet<>();

    Group(String id, String name) {
        this.id = id;
        this.name = name;
    }

    public String getId() {
        return id;
    }

    public String getName() {
        return name;
    }

    public Set<User> getUsers() {
        return Collections.unmodifiableSet(users);
    }
    public void addUser(User user){
        if(users.add(user)){
            user.addGroup(this);
        }
    }

    public void removeUser(User user){
        if(users.remove(user)){
            user.removeGroup(this);
        }
    }
    @Override
    public String toString() {
        return name + "(" + id + ")";
    }

}

/* ============================================================
                PERMISSION SUBJECT
   User OR Group
   ============================================================ */
interface PermissionSubject{
    String getId();
    String getName();
}

class UserSubject implements PermissionSubject{

    private final User user;

    UserSubject(User user) {
        this.user = user;
    }

    @Override
    public String getId() {
        return user.getId();
    }

    @Override
    public String getName() {
        return user.getName();
    }

    public User getUser(){
        return user;
    }
}

class GroupSubject implements PermissionSubject{

    private final Group group;

    GroupSubject(Group group) {
        this.group = group;
    }

    @Override
    public String getId() {
        return group.getId();
    }

    @Override
    public String getName() {
        return group.getName();
    }

    public Group getGroup(){
        return group;
    }
}

/* ============================================================
                PERMISSION ASSIGNMENT
   ============================================================ */

class PermissionAssignment{
    private final PermissionSubject subject;
    private final PermissionEffect effect;
    private final Permission permission;


    PermissionAssignment(PermissionSubject subject, PermissionEffect effect, Permission permission) {
        this.subject = subject;
        this.effect = effect;
        this.permission = permission;
    }

    public PermissionSubject getSubject() {
        return subject;
    }

    public PermissionEffect getEffect() {
        return effect;
    }

    public Permission getPermission() {
        return permission;
    }

    /*
     * Two assignments are considered the same if they target:
     *
     * subject + permission
     *
     * Effect is intentionally not part of equality because:
     *
     * READ ALLOW
     * READ DENY
     *
     * should be treated as conflicting assignments.
     */

    @Override
    public boolean equals(Object obj) {
        if (this == obj)return true;

        if(!(obj instanceof PermissionAssignment other))return false;

        return subject.getId().equals(other.subject.getId()) && permission == other.permission;
    }

    @Override
    public int hashCode(){
        return Objects.hash(subject.getId(),permission);
    }
}

/* ============================================================
                        RESOURCE
                  Composite Node
   ============================================================ */

class Resource {
    private final String name;
    private Resource parent;
    private final List<Resource> children = new ArrayList<>();
    private final Set<PermissionAssignment> assignments = new HashSet<>();

    Resource(String name) {
        this.name = name;
    }

    public String getName() {
        return name;
    }

    public Resource getParent() {
        return parent;
    }

    public List<Resource> getChildren() {
        return Collections.unmodifiableList(children);
    }

    public Set<PermissionAssignment> getAssignments() {
        return Collections.unmodifiableSet(assignments);
    }

    public void addChild(Resource child) {
        if (child == this) {
            throw new IllegalArgumentException("Resource cannot be its own child.");
        }
        if (child.parent != null) {
            throw new IllegalArgumentException("Resource already has a parent.");
        }
        if (children.contains(child)) {
            throw new IllegalArgumentException("Child Already exists.");
        }
        if (isDescendent(this, child)) {
            throw new IllegalArgumentException("Cannot create a cycle in resource hierarchy");
        }
        children.add(child);
        child.parent = this;
    }

    public void removeChild(Resource child) {
        if (children.remove(child)) {
            child.parent = null;
        }
    }

    private boolean isDescendent(Resource resource, Resource potentialDescendent) {
        Resource current = potentialDescendent;
        while (current != null) {
            if (current == resource) return true;

            current = current.parent;
        }

        return false;
    }

    public void assignPermission(PermissionSubject subject, PermissionEffect effect, Permission permission) {
        PermissionAssignment assignment = new PermissionAssignment(subject, effect, permission);
        /*
         * Same subject + same permission cannot be assigned twice.
         * Example:
         * READ ALLOW
         * READ DENY
         * at the same resource is rejected.
         */
        if (assignments.contains(assignment)) {
            throw new IllegalArgumentException("Permission already assigned for subject=" + subject.getName() + ", permission=" + permission);
        }

        assignments.add(assignment);
    }

    public void revokePermission(Permission permission, PermissionSubject subject) {
        assignments.removeIf(assignment -> assignment.getSubject().getId().equals(subject.getId())
                && assignment.getPermission() == permission);
    }
}

/* ============================================================
                    PERMISSION SERVICE
   ============================================================ */
class PermissionService {


    /*
     * Main permission check.
     */
    public boolean hasPermission(
            User user,
            Resource resource,
            Permission permission) {

        Objects.requireNonNull(user);
        Objects.requireNonNull(resource);
        Objects.requireNonNull(permission);

        Resource current = resource;

        /*
         * Start from the most specific resource.
         *
         * Example:
         *
         * ServiceA
         *    ↓
         * Backend
         *    ↓
         * Engineering
         *    ↓
         * Organization
         *
         * Therefore:
         *
         * ServiceA permission overrides Backend,
         * Backend overrides Engineering, etc.
         */
        while (current != null) {

            PermissionEffect effect =
                    findPermissionAtResource(
                            user,
                            current,
                            permission
                    );

            if (effect != null) {
                return effect == PermissionEffect.ALLOW;
            }

            current = current.getParent();
        }

        /*
         * Default deny.
         */
        return false;
    }


    /*
     * Find the effective permission at ONE resource.
     */
    private PermissionEffect findPermissionAtResource(
            User user,
            Resource resource,
            Permission permission) {

        boolean hasAllow = false;
        boolean hasDeny = false;

        for (PermissionAssignment assignment :
                resource.getAssignments()) {

            if (assignment.getPermission() != permission) {
                continue;
            }

            PermissionSubject subject =
                    assignment.getSubject();


            /*
             * Direct user permission.
             */
            if (subject instanceof UserSubject userSubject) {

                if (userSubject.getUser().getId()
                        .equals(user.getId())) {

                    if (assignment.getEffect()
                            == PermissionEffect.DENY) {

                        hasDeny = true;

                    } else {

                        hasAllow = true;
                    }
                }
            }
        }


        /*
         * Group permissions.
         */
        for (Group group : user.getGroups()) {

            for (PermissionAssignment assignment :
                    resource.getAssignments()) {

                if (assignment.getPermission() != permission) {
                    continue;
                }

                PermissionSubject subject =
                        assignment.getSubject();


                /*
                 * In this implementation, Group itself
                 * implements PermissionSubject.
                 */
                if (subject instanceof GroupSubject groupSubject) {

                    if (groupSubject.getGroup().getId()
                            .equals(group.getId())) {

                        if (assignment.getEffect()
                                == PermissionEffect.DENY) {

                            hasDeny = true;

                        } else {

                            hasAllow = true;
                        }
                    }
                }
            }
        }


        /*
         * Conflict rule:
         *
         * DENY > ALLOW
         */
        if (hasDeny) {
            return PermissionEffect.DENY;
        }

        if (hasAllow) {
            return PermissionEffect.ALLOW;
        }

        return null;
    }
}

/* ============================================================
                    TREE PRINTER
   ============================================================ */

class TreePrinter {

    public static void print(Resource resource) {
        print(resource, "");
    }

    private static void print(
            Resource resource,
            String indent) {

        System.out.println(
                indent + resource.getName()
        );

        for (Resource child :
                resource.getChildren()) {

            print(child, indent + "  ");
        }
    }
}

public class permissionControlAdvance {
    public static void main(String[] args) {
        PermissionService permissionService =
                new PermissionService();


        /* ====================================================
                            USERS
           ==================================================== */

        User tanish =
                new User("U1", "Tanish");

        User alice =
                new User("U2", "Alice");

        User bob =
                new User("U3", "Bob");


        /* ====================================================
                            GROUPS
           ==================================================== */

        Group engineering =
                new Group("G1", "Engineering");

        Group backend =
                new Group("G2", "Backend");

        Group admins =
                new Group("G3", "Admins");


        /* ====================================================
                        GROUP MEMBERSHIP
           ==================================================== */

        engineering.addUser(tanish);
        engineering.addUser(alice);

        backend.addUser(tanish);

        admins.addUser(bob);


        /* ====================================================
                        RESOURCE TREE
           ==================================================== */

        Resource organization =
                new Resource("Organization");

        Resource engineeringResource =
                new Resource("Engineering");

        Resource backendResource =
                new Resource("Backend Team");

        Resource frontendResource =
                new Resource("Frontend Team");

        Resource serviceA =
                new Resource("Service A");

        Resource serviceB =
                new Resource("Service B");


        organization.addChild(engineeringResource);

        engineeringResource.addChild(backendResource);
        engineeringResource.addChild(frontendResource);

        backendResource.addChild(serviceA);
        backendResource.addChild(serviceB);


        /* ====================================================
                    PERMISSIONS
           ==================================================== */

        /*
         * Engineering group:
         *
         * READ ALLOW on Engineering resource.
         *
         * Therefore all Engineering descendants inherit READ.
         */
        engineeringResource.assignPermission(
                new GroupSubject(engineering),
                PermissionEffect.ALLOW,
                Permission.READ
        );


        /*
         * Backend group:
         *
         * WRITE ALLOW on Backend.
         */
        backendResource.assignPermission(
                new GroupSubject(backend),
                PermissionEffect.ALLOW,
                Permission.WRITE
        );


        /*
         * Tanish gets DELETE directly on Service A.
         */
        serviceA.assignPermission(
                new UserSubject(tanish),
                PermissionEffect.ALLOW,
                Permission.DELETE

        );


        /*
         * Tanish gets READ DENY specifically on Service A.
         *
         * This overrides Engineering READ ALLOW
         * because Service A is more specific.
         */
        serviceA.assignPermission(
                new UserSubject(tanish),
                PermissionEffect.DENY,
                Permission.READ
        );


        /*
         * Admin group gets ADMIN permission
         * on the Engineering resource.
         */
        engineeringResource.assignPermission(
                new GroupSubject(admins),
                PermissionEffect.ALLOW,
                Permission.ADMIN
        );


        /* ====================================================
                        TEST PERMISSIONS
           ==================================================== */

        System.out.println(
                "Tanish READ Backend: "
                        + permissionService.hasPermission(
                        tanish,
                        backendResource,
                        Permission.READ
                )
        );

        System.out.println(
                "Tanish READ Service B: "
                        + permissionService.hasPermission(
                        tanish,
                        serviceB,
                        Permission.READ
                )
        );

        System.out.println(
                "Tanish READ Service A: "
                        + permissionService.hasPermission(
                        tanish,
                        serviceA,
                        Permission.READ
                )
        );

        System.out.println(
                "Tanish DELETE Service A: "
                        + permissionService.hasPermission(
                        tanish,
                        serviceA,
                        Permission.DELETE
                )
        );

        System.out.println(
                "Tanish WRITE Backend: "
                        + permissionService.hasPermission(
                        tanish,
                        backendResource,
                        Permission.WRITE
                )
        );

        System.out.println(
                "Bob ADMIN Backend: "
                        + permissionService.hasPermission(
                        bob,
                        backendResource,
                        Permission.ADMIN
                )
        );


        /* ====================================================
                        PRINT RESOURCE TREE
           ==================================================== */

        System.out.println();
        System.out.println("===== RESOURCE TREE =====");

        TreePrinter.print(organization);
    }
}
