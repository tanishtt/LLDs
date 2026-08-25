package com.tanish.lld.composite.OrganizationHierarchy;

import java.util.*;
//"The organization hierarchy is inherently a tree. Since both individual contributors and managers "
//"are employees and can be represented by the same Employee entity, I don't need to separate Leaf"
//" and Composite classes. The employee itself acts as the composite when it has direct reports."

//improvement
//public List<Employee> getDirectReports() {
//    return directReports;
//}
//getDirectReports() exposes internal state this allows to do employee.getDirectReports().clear();
//public void addReport(Employee employee) {
//    directReports.add(employee);
//    employee.setManager(this);
//}
//
//public void removeReport(Employee employee) {
//    directReports.remove(employee);
//    employee.setManager(null);
//}


class Employee{
    private final long id;
    private final String name;
    private final String designation;
    private final List<Employee> directReports;

    Employee(long id, String name, String designation) {

        this.id = id;
        this.name = name;
        this.designation = designation;
        directReports=new ArrayList<>();
    }

    private Employee manager;

    public long getId() {
        return id;
    }

    public String getName() {
        return name;
    }

    public String getDesignation() {
        return designation;
    }

    public List<Employee> getDirectReports() {
        return directReports;
    }

    public Employee getManager() {
        return manager;
    }

    public void setManager(Employee manager){
        this.manager=manager;
    }

    @Override
    public String toString(){
        return id+" - "+name+" ("+designation+") ";
    }

}

class OrganizationService{
    private final Employee ceo;
    private final Map<Long, Employee> employeeMap;

    public OrganizationService(Employee ceo) {
        this.ceo = ceo;
        employeeMap=new HashMap<>();
        employeeMap.put(ceo.getId(), ceo);
    }

    //add employee
    public void addEmployee(long id, String name, String designation, long managerId){
        if (employeeMap.containsKey(id)) {
            throw new IllegalArgumentException("Employee ID already exists");
        }
        Employee manager=employeeMap.get(managerId);
        if(manager == null){
            throw new IllegalArgumentException("Manager not found.");
        }

        Employee employee=new Employee(id, name, designation);
        employee.setManager(manager);
        manager.getDirectReports().add(employee);
        employeeMap.put(id, employee);
    }

    //remove employee
    public void removeEmployee(long employeeId){
        Employee employee=employeeMap.get(employeeId);
        if(employee == null){
            throw new IllegalArgumentException("employee not found.");
        }
        if(employee == ceo){
            throw new IllegalArgumentException("ceo can't be remove.");
        }

        Employee manager= employee.getManager();
        manager.getDirectReports().remove(employee);
        for (Employee child: employee.getDirectReports()){
            manager.getDirectReports().add(child);
            child.setManager(manager);
        }
        employeeMap.remove(employeeId);
    }

    //find manager
    public Employee getManager(long employeeId){
        Employee employee= employeeMap.get(employeeId);
        if (employee==null)return null;
        return employee.getManager();
    }
    //get direct reports
    public List<Employee> getDirectReports(long employeeId){
        Employee employee= employeeMap.get(employeeId);
        if (employee==null)return Collections.emptyList();
        return employee.getDirectReports();
    }
    //print reporting chain
    public List<Employee> getReportingChain(long employeeId){
        Employee current= employeeMap.get(employeeId);
        List<Employee> result=new ArrayList<>();
        while (current!=null){
            result.add(current);
            current=current.getManager();
        }

        return result;
    }
    //count employees working under
    public int countSubordinates(long employeeId){
        Employee employee=employeeMap.get(employeeId);
        if (employee==null){
            return 0;
        }
        return dfsCount(employee);
    }
    private int dfsCount(Employee employee){
        int count=0;
        for(Employee child: employee.getDirectReports()){
            count++;
            count+=dfsCount(child);
        }

        return count;
    }
    //move employee
    public void moveEmployee(long employeeId, long newManagerId){
        Employee employee=employeeMap.get(employeeId);
        Employee newManager=employeeMap.get(newManagerId);
        if(employee==null || newManager == null){
            throw new IllegalArgumentException("Both employee and manager are required.");
        }
        if(employee == ceo){
            throw new IllegalArgumentException("CEO cant be moved.");
        }

        Employee oldManager=employee.getManager();
        oldManager.getDirectReports().remove(employee);

        newManager.getDirectReports().add(employee);
        employee.setManager(newManager);

    }
    //lowest common manager
    public Employee findCommonManager(long employeeId1, long employeeId2){
        Employee e1=employeeMap.get(employeeId1);
        Employee e2=employeeMap.get(employeeId2);

        Set<Employee> ancestors=new HashSet<>();
        while (e1!=null){
            ancestors.add(e1);
            e1=e1.getManager();
        }

        while (e2!=null){
            if(ancestors.contains(e2)){
                return e2;
            }
            e2=e2.getManager();
        }
        return null;
    }

    //print hierarchy
    public void printHierarchy(){
        print(ceo, "");
    }
    void print(Employee employee, String indent){
        System.out.println(indent + employee);
        for (Employee child : employee.getDirectReports()){
            print(child, indent+"  ");
        }
    }

}


public class OrganizationDriver {
    public static void main(String[] args) {
        Employee ceo=new Employee(1,"tanish", "CEO");
        OrganizationService org=new OrganizationService(ceo);
        org.addEmployee(2,"Alice","VP Engineering",1);
        org.addEmployee(3,"Bob","VP Sales",1);
        org.addEmployee(4, "David", "Manager", 2);
        org.addEmployee(5, "Emma", "Engineer", 4);
        org.addEmployee(6, "Chris", "Engineer", 4);
        org.addEmployee(7, "Monty", "Data Science", 3);
        org.printHierarchy();

        System.out.println();
        System.out.println(org.findCommonManager(6,5));
        System.out.println();
        System.out.println(org.getReportingChain(6));
        System.out.println();
        System.out.println(org.countSubordinates(2));

        org.moveEmployee(4, 3);
        org.removeEmployee(5);
        org.printHierarchy();

    }
}
