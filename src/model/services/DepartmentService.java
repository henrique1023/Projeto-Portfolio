package model.services;

import java.util.List;

import model.dao.DaoFactory;
import model.dao.DepartmentDao;
import model.entities.Department;

public class DepartmentService {
	
	
	private DepartmentDao dao = new DaoFactory().createDepartmentDao();
	
	public List<Department> findAll(){
		return dao.findAll();
	}
	
	//esse metodo que salva no banco o objeto passado
	public void saveOrUptade(Department obj) {
		if(obj.getId() == null) {
			dao.insert(obj);
		}else {
			dao.update(obj);
		}
	}
	
	public void remove(Department obj) {
		dao.deleteById(obj.getId());
	}
}
