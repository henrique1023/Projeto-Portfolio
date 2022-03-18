package model.services;

import java.util.ArrayList;
import java.util.List;

import model.entities.Department;

public class DepartmentService {
	
	public List<Department> findAll(){
		List<Department> list = new ArrayList<>();
		list.add(new Department (1, "Eletronics"));
		list.add(new Department (2, "TI"));
		list.add(new Department (3, "Analist"));
		list.add(new Department (4, "Computer"));
		return list;
	}

}
