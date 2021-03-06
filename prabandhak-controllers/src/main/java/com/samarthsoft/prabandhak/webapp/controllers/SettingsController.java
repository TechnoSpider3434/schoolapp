package com.samarthsoft.prabandhak.webapp.controllers;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.validation.Valid;

import org.json.JSONArray;
import org.json.JSONObject;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.ModelMap;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.WebDataBinder;
import org.springframework.web.bind.annotation.InitBinder;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.servlet.ModelAndView;

import com.samarthsoft.prabandhak.common.DateUtility;
import com.samarthsoft.prabandhak.common.JsonConverter;
import com.samarthsoft.prabandhak.common.ModelAttributesCommon;
import com.samarthsoft.prabandhak.common.RequestCommon;
import com.samarthsoft.prabandhak.common.ValidatorCommon;
import com.samarthsoft.prabandhak.connector.DBCommunicator;
import com.samarthsoft.prabandhak.entities.Caste;
import com.samarthsoft.prabandhak.entities.Category;
import com.samarthsoft.prabandhak.entities.ClassInfo;
import com.samarthsoft.prabandhak.entities.ClassWiseSubjects;
import com.samarthsoft.prabandhak.entities.Exam;
import com.samarthsoft.prabandhak.entities.Filter;
import com.samarthsoft.prabandhak.entities.Nationality;
import com.samarthsoft.prabandhak.entities.PaginationObject;
import com.samarthsoft.prabandhak.entities.ScholarshipType;
import com.samarthsoft.prabandhak.entities.SchoolType;
import com.samarthsoft.prabandhak.entities.Student;
import com.samarthsoft.prabandhak.entities.Subject;
import com.samarthsoft.prabandhak.entities.SupportingStaffDesignations;
import com.samarthsoft.prabandhak.entities.Teacher;
import com.samarthsoft.prabandhak.entities.TeacherDesignations;
import com.samarthsoft.prabandhak.entity.comparators.StudentComparator;
import com.samarthsoft.prabandhak.enums.RestrictionType;
import com.samarthsoft.prabandhak.form.entities.ApplicationSession;
import com.samarthsoft.prabandhak.form.entities.ClassInfoFormObject;
import com.samarthsoft.prabandhak.form.entities.ExamSettingsForm;
import com.samarthsoft.prabandhak.utilities.CommonControllerUtility;
import com.samarthsoft.prabandhak.utilities.SettingsControllerUtility;
import com.samarthsoft.prabandhak.webapp.validators.SettingsValidator;

@Controller
public class SettingsController {
	@Autowired
	private SettingsControllerUtility settingsControllerUtility;
	@Autowired
	private CommonControllerUtility commonControllerUtility;
	@Autowired
	private SettingsValidator settingsValidator;
	
	@RequestMapping(value = "/settings", method = RequestMethod.GET)
	public ModelAndView viewSettings(HttpServletRequest request,
			HttpServletResponse response, ModelMap model) {
		return new ModelAndView("settings");
	}

	@RequestMapping(value = "/showcasts", method = RequestMethod.GET)
	public ModelAndView viewCastes(HttpServletRequest request,
			HttpServletResponse response, ModelMap model) {
		PaginationObject paginationObject = commonControllerUtility.getObjectsByClass(Caste.class,commonControllerUtility.getPageNumber(request));
		Map<String, String> categories = getCategories();
		List<Caste> castes = new ArrayList<Caste>();
		List<Object> casteObjects = paginationObject.getRecords();
		for (Object object : casteObjects) {
			Caste caste = (Caste)object;
			if(categories!=null && !categories.isEmpty())
				caste.setCategoryGuid(categories.get(caste.getCategoryGuid()));
			else caste.setCategoryGuid("-");
			castes.add(caste);
		}
		model.put("castes",castes);
		commonControllerUtility.setCommonAttributesOfPagination(paginationObject, model, commonControllerUtility.getPageNumber(request));
		return new ModelAndView("castes_list",model);
	}
	
	private Map<String, String> getCategories(){
		Map<String, String> result = new HashMap<String, String>();
		List<Category> categories = ModelAttributesCommon.getCategoriesForModelAttributes();
		for (Category category : categories) {
			result.put(category.getGuid(), category.getName());
		}
		return result;
	}
	
	@RequestMapping(value = "/showexams", method = RequestMethod.GET)
	public ModelAndView viewExams(HttpServletRequest request,
			HttpServletResponse response, ModelMap model) {
		PaginationObject paginationObject = commonControllerUtility.getObjectsByClass(Exam.class,commonControllerUtility.getPageNumber(request));
		model.put("exams",paginationObject.getRecords());
		commonControllerUtility.setCommonAttributesOfPagination(paginationObject, model, commonControllerUtility.getPageNumber(request));
		return new ModelAndView("exams_list",model);
	}
	
	@RequestMapping(value = "/addcaste", method = RequestMethod.GET)
	public ModelAndView addCaste(HttpServletRequest request,
			HttpServletResponse response, ModelMap model) {
		model.put("caste", new Caste());
		model.put("categories",ModelAttributesCommon.getCategoriesForModelAttributes());
		return new ModelAndView("caste_form",model);
	}
	
	@RequestMapping(value = "/editcaste", method = RequestMethod.GET)
	public ModelAndView editCaste(HttpServletRequest request,
			HttpServletResponse response, ModelMap model) {
		String id = RequestCommon.getAttributeValueFromRequest("id", request);
		if(id!=null && !id.equals(""))
			model.put("caste", DBCommunicator.getApiServices().getGenericApi().fetchObjectbyID(Caste.class, id));
		else
			model.put("caste", new Caste());
		model.put("categories",ModelAttributesCommon.getCategoriesForModelAttributes());
		return new ModelAndView("caste_form",model);
	}

	@RequestMapping(value = "/addcaste", method = RequestMethod.POST)
	public ModelAndView submitAddCasteForm(@Valid Caste caste,BindingResult bindingResult,
			HttpServletRequest request, HttpServletResponse response, ModelMap model) {
		return addEditCaste(caste, bindingResult, request, response, model,true);
	}
	
	private ModelAndView addEditCaste(Caste caste,BindingResult bindingResult,
			HttpServletRequest request, HttpServletResponse response, ModelMap model,Boolean isAddingRecord){
		if(!bindingResult.hasErrors()){
			Boolean result = false;
			if(isAddingRecord)
				result = DBCommunicator.getApiServices().getGenericApi().insert(caste);
			else result = DBCommunicator.getApiServices().getGenericApi().update(caste);
			if(result){
				return new ModelAndView("redirect:/showcasts.htm", model);
			}
		}
		model.put("caste", caste);
		model.put("categories",ModelAttributesCommon.getCategoriesForModelAttributes());
		ValidatorCommon.checkAndAddFieldErrors(bindingResult, model);
    	return new ModelAndView("caste_form",model);
	}

	@RequestMapping(value = "/editcaste", method = RequestMethod.POST)
	public ModelAndView submitEditCasteForm(@Valid Caste caste,BindingResult bindingResult,
			HttpServletRequest request, HttpServletResponse response, ModelMap model) {
		return addEditCaste(caste, bindingResult, request, response, model,false);
	}

	@RequestMapping(value = "/showcategories", method = RequestMethod.GET)
	public ModelAndView viewCategories(HttpServletRequest request,
			HttpServletResponse response, ModelMap model) {
		PaginationObject paginationObject = commonControllerUtility.getObjectsByClass(Category.class,commonControllerUtility.getPageNumber(request));
		commonControllerUtility.setCommonAttributesOfPagination(paginationObject, model, commonControllerUtility.getPageNumber(request));
		model.put("categories", paginationObject.getRecords());
		return new ModelAndView("categories_list",model);
	}
	
	@RequestMapping(value = "/addcategory", method = RequestMethod.GET)
	public ModelAndView addCategory(HttpServletRequest request,
			HttpServletResponse response, ModelMap model) {
		model.put("category", new Category());
		return new ModelAndView("category_form",model);
	}
	
	@RequestMapping(value = "/editcategory", method = RequestMethod.GET)
	public ModelAndView editCategory(HttpServletRequest request,
			HttpServletResponse response, ModelMap model) {
		String id = RequestCommon.getAttributeValueFromRequest("id", request);
		if(id!=null && !id.equals(""))
			model.put("category", DBCommunicator.getApiServices().getGenericApi().fetchObjectbyID(Category.class, id));
		else
			model.put("category", new ScholarshipType());
		return new ModelAndView("category_form",model);
	}

	@RequestMapping(value = "/addcategory", method = RequestMethod.POST)
	public ModelAndView submitAddCategory(@Valid Category category,BindingResult bindingResult,
			HttpServletRequest request, HttpServletResponse response, ModelMap model) {
		return addEditCategory(category, bindingResult, request, response, model,true);
	}
	
	@RequestMapping(value = "/editcategory", method = RequestMethod.POST)
	public ModelAndView submitEditCategoryForm(@Valid Category category,BindingResult bindingResult,
			HttpServletRequest request, HttpServletResponse response, ModelMap model) {
		return addEditCategory(category, bindingResult, request, response, model,false);
	}

	@RequestMapping(value = "/showscholarshiptypes", method = RequestMethod.GET)
	public ModelAndView viewScholarshipTypes(HttpServletRequest request,
			HttpServletResponse response, ModelMap model) {
		PaginationObject paginationObject = commonControllerUtility.getObjectsByClass(ScholarshipType.class,commonControllerUtility.getPageNumber(request));
		commonControllerUtility.setCommonAttributesOfPagination(paginationObject, model, commonControllerUtility.getPageNumber(request));
		model.put("scholarshiptypes", paginationObject.getRecords());
		return new ModelAndView("scholarshiptypes_list",model);
	}
	
	@RequestMapping(value = "/addscholarshiptype", method = RequestMethod.GET)
	public ModelAndView addScholarshipType(HttpServletRequest request,
			HttpServletResponse response, ModelMap model) {
		model.put("scholarshiptype", new ScholarshipType());
		return new ModelAndView("scholarshiptype_form",model);
	}
	
	@RequestMapping(value = "/editscholarshiptype", method = RequestMethod.GET)
	public ModelAndView editScholarshipType(HttpServletRequest request,
			HttpServletResponse response, ModelMap model) {
		String id = RequestCommon.getAttributeValueFromRequest("id", request);
		if(id!=null && !id.equals(""))
			model.put("scholarshiptype", DBCommunicator.getApiServices().getGenericApi().fetchObjectbyID(ScholarshipType.class, id));
		else
			model.put("scholarshiptype", new ScholarshipType());
		return new ModelAndView("scholarshiptype_form",model);
	}

	@RequestMapping(value = "/addscholarshiptype", method = RequestMethod.POST)
	public ModelAndView submitAddScholarshipTypeForm(@Valid ScholarshipType scholarshipType,BindingResult bindingResult,
			HttpServletRequest request, HttpServletResponse response, ModelMap model) {
		return addEditScholarshipType(scholarshipType, bindingResult, request, response, model,true);
	}
	
	@RequestMapping(value = "/editscholarshiptype", method = RequestMethod.POST)
	public ModelAndView submitEditScholarshipTypeForm(@Valid ScholarshipType scholarshipType,BindingResult bindingResult,
			HttpServletRequest request, HttpServletResponse response, ModelMap model) {
		return addEditScholarshipType(scholarshipType, bindingResult, request, response, model,true);
	}
	
	private ModelAndView addEditScholarshipType(ScholarshipType scholarshipType,BindingResult bindingResult,
			HttpServletRequest request, HttpServletResponse response, ModelMap model,Boolean isAddingRecord){
		if(!bindingResult.hasErrors()){
			Boolean result = false;
			if(isAddingRecord)
				result = DBCommunicator.getApiServices().getGenericApi().insert(scholarshipType);
			else result = DBCommunicator.getApiServices().getGenericApi().update(scholarshipType);
			if(result){
				return new ModelAndView("redirect:/showscholarshiptypes.htm", model);
			}
		}
		model.put("scholarshiptype", scholarshipType);
		ValidatorCommon.checkAndAddFieldErrors(bindingResult, model);
		return new ModelAndView("scholarshiptype_form",model);
	}

	@RequestMapping(value = "/showschooltypes", method = RequestMethod.GET)
	public ModelAndView viewSchoolTypes(HttpServletRequest request,
			HttpServletResponse response, ModelMap model) {
		PaginationObject paginationObject = commonControllerUtility.getObjectsByClass(SchoolType.class,commonControllerUtility.getPageNumber(request));
		commonControllerUtility.setCommonAttributesOfPagination(paginationObject, model, commonControllerUtility.getPageNumber(request));
		model.put("schooltypes", paginationObject.getRecords());
		return new ModelAndView("schooltypes_list",model);
	}
	
	@RequestMapping(value = "/addschooltype", method = RequestMethod.GET)
	public ModelAndView addSchoolType(HttpServletRequest request,
			HttpServletResponse response, ModelMap model) {
		model.put("schooltype", new SchoolType());
		return new ModelAndView("schooltype_form",model);
	}
	
	@RequestMapping(value = "/editschooltype", method = RequestMethod.GET)
	public ModelAndView editSchoolType(HttpServletRequest request,
			HttpServletResponse response, ModelMap model) {
		String id = RequestCommon.getAttributeValueFromRequest("id", request);
		if(id!=null && !id.equals(""))
			model.put("schooltype", DBCommunicator.getApiServices().getGenericApi().fetchObjectbyID(SchoolType.class, id));
		else
			model.put("schooltype", new SchoolType());
		return new ModelAndView("schooltype_form",model);
	}

	@RequestMapping(value = "/addschooltype", method = RequestMethod.POST)
	public ModelAndView submitAddSchoolTypeForm(@Valid SchoolType schoolType,BindingResult bindingResult,
			HttpServletRequest request, HttpServletResponse response, ModelMap model) {
		return addEditSchoolType(schoolType, bindingResult, request, response, model,true);
	}
	
	@RequestMapping(value = "/editschooltype", method = RequestMethod.POST)
	public ModelAndView submitEditSchoolTypeForm(@Valid SchoolType schoolType,BindingResult bindingResult,
			HttpServletRequest request, HttpServletResponse response, ModelMap model) {
		return addEditSchoolType(schoolType, bindingResult, request, response, model,false);
	}
	
	
	private ModelAndView addEditSchoolType(SchoolType schoolType,BindingResult bindingResult,
			HttpServletRequest request, HttpServletResponse response, ModelMap model,Boolean isAddingRecord){
		if(!bindingResult.hasErrors()){
			Boolean result = false;
			if(isAddingRecord)
				result = DBCommunicator.getApiServices().getGenericApi().insert(schoolType);
			else result = DBCommunicator.getApiServices().getGenericApi().update(schoolType);
			if(result){
				return new ModelAndView("redirect:/showschooltypes.htm", model);
			}
		}
		model.put("schooltype", schoolType);
		ValidatorCommon.checkAndAddFieldErrors(bindingResult, model);
		return new ModelAndView("schooltype_form",model);
	}

	
	private ModelAndView addEditCategory(Category category,BindingResult bindingResult,
			HttpServletRequest request, HttpServletResponse response, ModelMap model,Boolean isAddingRecord){
		if(!bindingResult.hasErrors()){
			Boolean result = false;
			if(isAddingRecord)
				result = DBCommunicator.getApiServices().getGenericApi().insert(category);
			else result = DBCommunicator.getApiServices().getGenericApi().update(category);
			if(result){
				return new ModelAndView("redirect:/showcategories.htm", model);
			}
		}
		model.put("category", category);
		ValidatorCommon.checkAndAddFieldErrors(bindingResult, model);
		return new ModelAndView("category_form",model);
	}

	@RequestMapping(value = "/showclasses", method = RequestMethod.GET)
	public ModelAndView viewClasses(HttpServletRequest request,
			HttpServletResponse response, ModelMap model) {
    	int pageNumber = 1;
    	if(RequestCommon.getAttributeValueFromRequest("page", request)!=null && !RequestCommon.getAttributeValueFromRequest("page", request).isEmpty())
    		pageNumber = Integer.parseInt(RequestCommon.getAttributeValueFromRequest("page", request));
		PaginationObject paginationObject = commonControllerUtility.getObjectsByClass(ClassInfo.class,commonControllerUtility.getPageNumber(request));
		List<Object> objects = paginationObject.getRecords();
		List<ClassInfo> standards = new ArrayList<ClassInfo>();
		Map<String, String> schoolTypes = getSchoolTypesMap();
		for (Object object : objects) {
			ClassInfo classInfo = (ClassInfo) object;
			if(schoolTypes!=null && !schoolTypes.isEmpty())
				classInfo.setSchoolTypeGuid(schoolTypes.get(classInfo.getSchoolTypeGuid()));
			else classInfo.setSchoolTypeGuid("-");
			standards.add(classInfo);
		}
		model.put("classes", standards);
		commonControllerUtility.setCommonAttributesOfPagination(paginationObject, model, pageNumber);
		return new ModelAndView("classes_list",model);
	}

	private Map<String, String> getSchoolTypesMap(){
		Map<String, String> result = new HashMap<String, String>();
		List<Object> schoolTypes = ModelAttributesCommon.getSchoolTypesForModelAttributes();
		for (Object object : schoolTypes) {
			SchoolType schoolType = (SchoolType) object;
			result.put(schoolType.getGuid(), schoolType.getName());
		}
		return result;
	}
	
	@RequestMapping(value = "/addclass", method = RequestMethod.GET)
	public ModelAndView addClass(HttpServletRequest request,
			HttpServletResponse response, ModelMap model) {
		ClassInfoFormObject classInfoFormObject = new ClassInfoFormObject();
		classInfoFormObject.setClassInfo(new ClassInfo());
		model.put("classinfo", classInfoFormObject);
		model.put("schoolTypes",ModelAttributesCommon.getSchoolTypesForModelAttributes());
		model.put("subjects", getSubjectJson(ModelAttributesCommon.getSubjectsForModelAttributes()));
		List<Filter> filters = new ArrayList<Filter>();
		filters.add(new Filter("schoolGuid", RequestCommon.getApplicationSession(request).getSchoolGuid(), RestrictionType.EQ));
		model.put("teachers",getTeacherJson(DBCommunicator.getApiServices().getGenericApi().getFilteredList(Teacher.class, filters, null)));
		model.put("teachers_list",DBCommunicator.getApiServices().getGenericApi().getFilteredList(Teacher.class, filters, null));
		model.put("subjects_list",ModelAttributesCommon.getSubjectsForModelAttributes());
		return new ModelAndView("class_form",model);
	}
	
	@RequestMapping(value = "/editclass", method = RequestMethod.GET)
	public ModelAndView editClass(HttpServletRequest request,
			HttpServletResponse response, ModelMap model) {
		String id = RequestCommon.getAttributeValueFromRequest("id", request);
		if(id!=null && !id.equals("")){
			ClassInfoFormObject classInfoFormObject = settingsControllerUtility.getClassInfoFormObject((ClassInfo)DBCommunicator.getApiServices().getGenericApi().fetchObjectbyID(ClassInfo.class, id));
			model.put("classinfo", classInfoFormObject);
		} else{
			ClassInfoFormObject classInfoFormObject = new ClassInfoFormObject();
			classInfoFormObject.setClassInfo(new ClassInfo());
			model.put("classinfo", classInfoFormObject);
		}
		model.put("schoolTypes",ModelAttributesCommon.getSchoolTypesForModelAttributes());
		model.put("subjects", getSubjectJson(ModelAttributesCommon.getSubjectsForModelAttributes()));
		List<Filter> filters = new ArrayList<Filter>();
		filters.add(new Filter("schoolGuid", RequestCommon.getApplicationSession(request).getSchoolGuid(), RestrictionType.EQ));
		model.put("teachers",getTeacherJson(DBCommunicator.getApiServices().getGenericApi().getFilteredList(Teacher.class, filters, null)));
		model.put("teachers_list",DBCommunicator.getApiServices().getGenericApi().getFilteredList(Teacher.class, filters, null));
		model.put("subjects_list",ModelAttributesCommon.getSubjectsForModelAttributes());
		return new ModelAndView("class_form", model);
	}
	
	@RequestMapping(value = "/addclass", method = RequestMethod.POST)
	public ModelAndView submitAddClassForm(@Valid ClassInfoFormObject classInfo,BindingResult bindingResult,
			HttpServletRequest request, HttpServletResponse response, ModelMap model) {
		return addEditClassInfo(classInfo, bindingResult, request, response, model,true);
	}
	
	private ModelAndView addEditClassInfo(ClassInfoFormObject classInfoFormObject, BindingResult bindingResult,
			HttpServletRequest request, HttpServletResponse response, ModelMap model,Boolean isAddingRecord){
		if(!bindingResult.hasErrors()){
			Boolean result = false;
			if(isAddingRecord)
				result = DBCommunicator.getApiServices().getGenericApi().insert(classInfoFormObject.getClassInfo());
			else result = DBCommunicator.getApiServices().getGenericApi().update(classInfoFormObject.getClassInfo());
			if(result){
				return new ModelAndView("redirect:/showclasses.htm", model);
			}
		}
		model.put("classinfo", classInfoFormObject.getClassInfo());
		model.put("schoolTypes",ModelAttributesCommon.getSchoolTypesForModelAttributes());
		ValidatorCommon.checkAndAddFieldErrors(bindingResult, model);
		return new ModelAndView("class_form",model);
	}

	@RequestMapping(value = "/editclass", method = RequestMethod.POST)
	public ModelAndView submitEditClassForm(@Valid ClassInfoFormObject classInfo,BindingResult bindingResult,
			HttpServletRequest request, HttpServletResponse response, ModelMap model) {
		return addEditClassInfo(classInfo, bindingResult, request, response, model,false);
	}

	@RequestMapping(value = "/loadcastes", method = RequestMethod.GET)
    @ResponseBody
    public String loadCastes(HttpServletRequest request, HttpServletResponse response){
		return JsonConverter.toJson(commonControllerUtility.getObjectsByClass(Caste.class,commonControllerUtility.getPageNumber(request)));
	}
	
    @RequestMapping(value = "/saveorupdatecaste", method = RequestMethod.GET)
    @ResponseBody
    public Boolean saveOrUpdateCaste(HttpServletRequest request, HttpServletResponse response) {
    	Boolean result = false;
    	String casteGuid = request.getParameter("casteGuid");
    	String categoryGuid = request.getParameter("categoryGuid");
    	String religion = request.getParameter("religion");
    	String subcaste = request.getParameter("subcaste");
    	Boolean isMinority = Boolean.valueOf(request.getParameter("isMinority"));
    	Caste caste = new Caste(categoryGuid,religion,subcaste,isMinority);
    	if(casteGuid!=null && !casteGuid.isEmpty()){
    		caste.setGuid(casteGuid); 
    	}
    	result = DBCommunicator.getApiServices().getGenericApi().insertOrUpdate(caste);
    	return result;
	}
    
	@RequestMapping(value = "/loadexams", method = RequestMethod.GET)
    @ResponseBody
    public String loadExams(HttpServletRequest request, HttpServletResponse response){
		return JsonConverter.toJson(DBCommunicator.getApiServices().getGenericApi().getFilteredList(Exam.class, null,null));
	}

    @RequestMapping(value = "/saveorupdateexam", method = RequestMethod.GET)
    @ResponseBody
    public Boolean saveOrUpdateExam(HttpServletRequest request, HttpServletResponse response) {
    	Boolean result = false;
    	String startDate = request.getParameter("startdate");
    	String endDate = request.getParameter("enddate");
    	String examName = request.getParameter("name");
    	Exam exam = new Exam();
    	//exam.setClassGuid(RequestCommon.getApplicationSession(request).getAssociatedClassGuid());
    	exam.setExamStartDate(DateUtility.convertStringToTime(startDate));
    	exam.setExamEndDate(DateUtility.convertStringToTime(endDate));
    	exam.setName(examName);
    	exam.setOrganizationGuid(RequestCommon.getApplicationSession(request).getSchoolGuid());
    	result = DBCommunicator.getApiServices().getGenericApi().insertOrUpdate(exam);
    	return result;
	}

	@RequestMapping(value = "/loadcategories", method = RequestMethod.GET)
    @ResponseBody
    public String loadCategories(HttpServletRequest request, HttpServletResponse response){
		return JsonConverter.toJson(commonControllerUtility.getObjectsByClass(Category.class,commonControllerUtility.getPageNumber(request)));
	}
    
    @RequestMapping(value = "/saveorupdatecategory", method = RequestMethod.GET)
    @ResponseBody
    public Boolean saveOrUpdateCategory(HttpServletRequest request, HttpServletResponse response) {
    	Boolean result = false;
    	String categoryGuid = request.getParameter("guid");
    	String categoryName = request.getParameter("name");
    	Category category = new Category();
    	category.setName(categoryName);
    	if(categoryGuid!=null && !categoryGuid.isEmpty()){
    		category.setGuid(categoryGuid); 
    	}
    	result = DBCommunicator.getApiServices().getGenericApi().insertOrUpdate(category);
    	return result;
	}
    
	@RequestMapping(value = "/loadclasses", method = RequestMethod.GET)
    @ResponseBody
    public String loadClasses(HttpServletRequest request, HttpServletResponse response){
		return JsonConverter.toJson(commonControllerUtility.getObjectsByClass(ClassInfo.class,commonControllerUtility.getPageNumber(request)));
	}
    
    @RequestMapping(value = "/saveorupdateclass", method = RequestMethod.GET)
    @ResponseBody
    public Boolean saveOrUpdateClass(HttpServletRequest request, HttpServletResponse response) {
    	Boolean result = false;
    	String classGuid = request.getParameter("guid");
    	String standardOrClass = request.getParameter("standardOrClass");
    	String division = request.getParameter("division");
    	String schoolTypeGuid = request.getParameter("schoolTypeGuid");
    	ClassInfo classInfo = new ClassInfo();
    	if(classGuid!=null && !classGuid.isEmpty()){
    		classInfo.setGuid(classGuid); 
    	}
		classInfo.setStandardOrClass(standardOrClass);
    	classInfo.setDivision(division);
    	classInfo.setSchoolTypeGuid(schoolTypeGuid);
    	ApplicationSession applicationSession = RequestCommon.getApplicationSession(request);
    	classInfo.setOrganizationGuid(applicationSession.getSchoolGuid());
    	result = DBCommunicator.getApiServices().getGenericApi().insertOrUpdate(classInfo);
    	return result;
	}
    
	@RequestMapping(value = "/loadscholarshiptypes", method = RequestMethod.GET)
    @ResponseBody
    public String loadScholarShipTypes(HttpServletRequest request, HttpServletResponse response){
		return JsonConverter.toJson(commonControllerUtility.getObjectsByClass(ScholarshipType.class,commonControllerUtility.getPageNumber(request)));
	}
    
    @RequestMapping(value = "/saveorupdatescholarshiptype", method = RequestMethod.GET)
    @ResponseBody
    public Boolean saveOrUpdateScholashipType(HttpServletRequest request, HttpServletResponse response) {
    	Boolean result = false;
    	String scholarshipTypeGuid = request.getParameter("guid");
    	String scholarshipTypeName = request.getParameter("name");
    	ScholarshipType scholarshipType = new ScholarshipType();
    	scholarshipType.setName(scholarshipTypeName);
    	if(scholarshipTypeGuid!=null && !scholarshipTypeGuid.isEmpty()){
    		scholarshipType.setGuid(scholarshipTypeGuid); 
    	}
    	result = DBCommunicator.getApiServices().getGenericApi().insertOrUpdate(scholarshipType);
    	return result;
	}
    
    @RequestMapping(value = "/loadclasswisesubjects", method = RequestMethod.GET)
    @ResponseBody
    public String loadClasswiseSubjects(HttpServletRequest request, HttpServletResponse response){
		return "";
	}
    
    @RequestMapping(value = "/saveorupdateclasswisesubjects", method = RequestMethod.GET)
    @ResponseBody
    public Boolean saveOrUpdateClassWiseSubjectDetails(HttpServletRequest request, HttpServletResponse response) {
    	Boolean result = false;
    	String subjectNames = request.getParameter("subjects");
    	ApplicationSession applicationSession = RequestCommon.getApplicationSession(request);
    	ClassWiseSubjects classWiseSubjects = new ClassWiseSubjects();
    	classWiseSubjects.setSubjects(subjectNames);
    	classWiseSubjects.setOrganizationGuid(applicationSession.getSchoolGuid());
    	//TODO:Set class GUID
    	classWiseSubjects.setGuid("");
    	result = DBCommunicator.getApiServices().getGenericApi().insertOrUpdate(classWiseSubjects);
    	return result;
	}
    
    @RequestMapping(value = "/updaterollnumbers", method = RequestMethod.GET)
    @ResponseBody
    public Boolean UpdateRollNumbersOfClass(HttpServletRequest request, HttpServletResponse response) {
    	Boolean result = false;
    	String classAndDivision = request.getParameter("class");
    	List<Filter> filters = new ArrayList<Filter>();
    	filters.add(new Filter("standard", classAndDivision.split("-")[0].trim(), RestrictionType.EQ));
    	filters.add(new Filter("division", classAndDivision.split("-")[1].trim(), RestrictionType.EQ));
    	/*List<SelectValues> selectValues = new ArrayList<SelectValues>();
    	selectValues.add(new SelectValues("name"));
    	selectValues.add(new SelectValues("guid"));*/
    	List<Object> students = DBCommunicator.getApiServices().getGenericApi().getFilteredList(Student.class, filters, null);
    	Collections.sort(students,new StudentComparator());
    	int count = 1;
    	List<Object> listToUpdate = new ArrayList<Object>();
    	for (Object object : students) {
			Student student = (Student) object;
			student.setRollNumber(""+count++);
			listToUpdate.add(student);
		}
    	result = DBCommunicator.getApiServices().getGenericApi().bulkUpdate(listToUpdate);
    	return result;
	}
    
	@ModelAttribute(value="classes")
    private List<ClassInfo> setClassesAndDivisionInModel(HttpServletRequest request){
    	ApplicationSession applicationSession = RequestCommon.getApplicationSession(request);
    	return ModelAttributesCommon.getClassInfoForModelAttributes(applicationSession);
    }
	
	@ModelAttribute(value="classesonly")
    private Set<String> setClassesInModel(HttpServletRequest request){
    	ApplicationSession applicationSession = RequestCommon.getApplicationSession(request);
    	return ModelAttributesCommon.getClassesForModelAttributes(applicationSession);
    }

	@RequestMapping(value = "/showsubjects", method = RequestMethod.GET)
	public ModelAndView viewSubjects(HttpServletRequest request,
			HttpServletResponse response, ModelMap model) {
		PaginationObject paginationObject = commonControllerUtility.getObjectsByClass(Subject.class,commonControllerUtility.getPageNumber(request));
		model.put("subjects",paginationObject.getRecords());
		commonControllerUtility.setCommonAttributesOfPagination(paginationObject, model, commonControllerUtility.getPageNumber(request));
		return new ModelAndView("subjects_list",model);
	}
	
	@RequestMapping(value = "/addsubject", method = RequestMethod.GET)
	public ModelAndView addSubject(HttpServletRequest request,
			HttpServletResponse response, ModelMap model) {
		model.put("subject", new Subject());
		return new ModelAndView("subject_form",model);
	}
	
	@RequestMapping(value = "/editsubject", method = RequestMethod.GET)
	public ModelAndView editSubject(HttpServletRequest request,
			HttpServletResponse response, ModelMap model) {
		String id = RequestCommon.getAttributeValueFromRequest("id", request);
		if(id!=null && !id.equals("")){
			Subject subject = (Subject)DBCommunicator.getApiServices().getGenericApi().fetchObjectbyID(Subject.class, id);
			model.put("subject", subject);
			if(subject.getDefaultForClasses()!=null && subject.getDefaultForClasses().contains(","))
				model.put("subjectforclasses",Arrays.asList(subject.getDefaultForClasses().split(","))); 
		}
		else
			model.put("subject", new Subject());
		return new ModelAndView("subject_form",model);
	}

	@RequestMapping(value = "/addsubject", method = RequestMethod.POST)
	public ModelAndView submitAddSubjectForm(@Valid Subject subject,BindingResult bindingResult,
			HttpServletRequest request, HttpServletResponse response, ModelMap model) {
		return addEditSubject(subject, bindingResult, request, response, model,true);
	}
	
	@RequestMapping(value = "/editsubject", method = RequestMethod.POST)
	public ModelAndView submitEditSubjectForm(@Valid Subject subject,BindingResult bindingResult,
			HttpServletRequest request, HttpServletResponse response, ModelMap model) {
		return addEditSubject(subject, bindingResult, request, response, model,false);
	}

	private ModelAndView addEditSubject(Subject subject,BindingResult bindingResult,
			HttpServletRequest request, HttpServletResponse response, ModelMap model,Boolean isAddingRecord){
		if(!bindingResult.hasErrors()){
			Boolean result = false;
			if(isAddingRecord)
				result = DBCommunicator.getApiServices().getGenericApi().insert(subject);
			else result = DBCommunicator.getApiServices().getGenericApi().update(subject);
			if(result){
				return new ModelAndView("redirect:/showsubjects.htm", model);
			}
		}
		model.put("subject", subject);
		if(subject.getDefaultForClasses()!=null)
			model.put("subjectforclasses",Arrays.asList(subject.getDefaultForClasses().split(",")));
		ValidatorCommon.checkAndAddFieldErrors(bindingResult, model);
		return new ModelAndView("subject_form",model);
	}
	
	//Exam add edit
	
	@RequestMapping(value = "/addexam", method = RequestMethod.GET)
	public ModelAndView addExam(HttpServletRequest request,
			HttpServletResponse response, ModelMap model) {
		model.put("exam", settingsControllerUtility.getExamSettingsFormObject(new Exam()));
		List<Subject> subjects = ModelAttributesCommon.getSubjectsForModelAttributes();
		model.put("subjects",getSubjectJson(subjects));
		model.put("subjectList",subjects);
		return new ModelAndView("exam_form",model);
	}
	
	@RequestMapping(value = "/editexam", method = RequestMethod.GET)
	public ModelAndView editExam(HttpServletRequest request,
			HttpServletResponse response, ModelMap model) {
		String id = RequestCommon.getAttributeValueFromRequest("id", request);
		if(id!=null && !id.equals("")){
			ExamSettingsForm examSettingsForm = settingsControllerUtility.getExamSettingsFormObject((Exam)DBCommunicator.getApiServices().getGenericApi().fetchObjectbyID(Exam.class, id));
			model.put("exam", examSettingsForm);
			model.put("subjectAndMarks",examSettingsForm.getSubjectAndMarks());
		}
		else
			model.put("exam", settingsControllerUtility.getExamSettingsFormObject(new Exam()));
		List<Subject> subjects = ModelAttributesCommon.getSubjectsForModelAttributes();
		model.put("subjects",getSubjectJson(subjects));
		model.put("subjectList",subjects);
		return new ModelAndView("exam_form",model);
	}

	private String getSubjectJson(List<Subject> subjects){
		String result = "";
		try{
			result = JsonConverter.toJson(subjects);
		}catch(Exception ex){
			ex.printStackTrace();
		}
		return result;
	}
	
	private String getTeacherJson(List<Object> teachers){
		String result = "";
		try{
			JSONArray jsonArr = new JSONArray();
			for (Object object : teachers) {
				Teacher teacher = (Teacher) object;
				JSONObject jsonObject = new JSONObject();
				jsonObject.append("guid", teacher.getGuid());
				jsonObject.append("name", teacher.getName().toString());
				jsonArr.put(jsonObject);
			}
			if(jsonArr!=null && jsonArr.length() > 0)
				result = jsonArr.toString();
		}catch(Exception ex){
			ex.printStackTrace();
		}
		return result;
	}


	@RequestMapping(value = "/addexam", method = RequestMethod.POST)
	public ModelAndView submitAddExamForm(@Valid ExamSettingsForm examFormObject,BindingResult bindingResult,
			HttpServletRequest request, HttpServletResponse response, ModelMap model) {
		return addEditExam(examFormObject, bindingResult, request, response, model,true);
	}
	
	@RequestMapping(value = "/editexam", method = RequestMethod.POST)
	public ModelAndView submitEditExamForm(@Valid ExamSettingsForm examFormObject,BindingResult bindingResult,
			HttpServletRequest request, HttpServletResponse response, ModelMap model) {
		return addEditExam(examFormObject, bindingResult, request, response, model,false);
	}

	private ModelAndView addEditExam(ExamSettingsForm examFormObject,BindingResult bindingResult,
			HttpServletRequest request, HttpServletResponse response, ModelMap model,Boolean isAddingRecord){
		if(!bindingResult.hasErrors()){
			Boolean result = false;
			if(examFormObject.getEnteredSubjectAndMarks()!=null)
				examFormObject.setEnteredSubjectAndMarks("["+examFormObject.getEnteredSubjectAndMarks().substring(0,examFormObject.getEnteredSubjectAndMarks().length()-1)+"]");
			if(isAddingRecord)
				result = DBCommunicator.getApiServices().getGenericApi().insert(settingsControllerUtility.getExamObject(examFormObject));
			else result = DBCommunicator.getApiServices().getGenericApi().update(settingsControllerUtility.getExamObject(examFormObject));
			if(result){
				return new ModelAndView("redirect:/showexams.htm", model);
			}
		}
		model.put("exam", examFormObject);
		ValidatorCommon.checkAndAddFieldErrors(bindingResult, model);
		List<Subject> subjects = ModelAttributesCommon.getSubjectsForModelAttributes();
		model.put("subjects",getSubjectJson(subjects));
		model.put("subjectList",subjects);
		return new ModelAndView("exam_form",model);
	}
	
	@RequestMapping(value = "/nationalities", method = RequestMethod.GET)
	public ModelAndView viewNationalities(HttpServletRequest request,
			HttpServletResponse response, ModelMap model) {
		PaginationObject paginationObject = commonControllerUtility.getObjectsByClass(Nationality.class,commonControllerUtility.getPageNumber(request));
		model.put("nationalities",paginationObject.getRecords());
		commonControllerUtility.setCommonAttributesOfPagination(paginationObject, model, commonControllerUtility.getPageNumber(request));
		return new ModelAndView("nationalities_list",model);
	}
	
	@RequestMapping(value = "/addnationality", method = RequestMethod.GET)
	public ModelAndView addNationality(HttpServletRequest request,
			HttpServletResponse response, ModelMap model) {
		model.put("nationality", new Nationality());
		return new ModelAndView("nationality_form",model);
	}
	
	@RequestMapping(value = "/editnationality", method = RequestMethod.GET)
	public ModelAndView editNationality(HttpServletRequest request,
			HttpServletResponse response, ModelMap model) {
		String id = RequestCommon.getAttributeValueFromRequest("id", request);
		if(id!=null && !id.equals(""))
			model.put("nationality", DBCommunicator.getApiServices().getGenericApi().fetchObjectbyID(Nationality.class, id));
		else
			model.put("nationality", new Caste());
		return new ModelAndView("nationality_form",model);
	}

	@RequestMapping(value = "/addnationality", method = RequestMethod.POST)
	public ModelAndView submitAddNationalityForm(@Valid Nationality nationality, BindingResult bindingResult,
			HttpServletRequest request, HttpServletResponse response, ModelMap model) {
		return addEditNationality(nationality, bindingResult, request, response, model,true);
	}
	
	private ModelAndView addEditNationality(Nationality nationality, BindingResult bindingResult,
			HttpServletRequest request, HttpServletResponse response, ModelMap model,Boolean isAddingRecord){
		if(!bindingResult.hasErrors()){
			Boolean result = false;
			if(isAddingRecord)
				result = DBCommunicator.getApiServices().getGenericApi().insert(nationality);
			else result = DBCommunicator.getApiServices().getGenericApi().update(nationality);
			if(result){
				return new ModelAndView("redirect:/nationalities.htm", model);
			}
		}
		model.put("nationality", nationality);
		ValidatorCommon.checkAndAddFieldErrors(bindingResult, model);
    	return new ModelAndView("nationality_form",model);
	}

	@RequestMapping(value = "/editnationality", method = RequestMethod.POST)
	public ModelAndView submitEditNationalityForm(@Valid Nationality nationality, BindingResult bindingResult,
			HttpServletRequest request, HttpServletResponse response, ModelMap model) {
		return addEditNationality(nationality, bindingResult, request, response, model,false);
	}

	@RequestMapping(value = "/teacherdesignations", method = RequestMethod.GET)
	public ModelAndView viewTeacherDesignations(HttpServletRequest request,
			HttpServletResponse response, ModelMap model) {
		PaginationObject paginationObject = commonControllerUtility.getObjectsByClass(TeacherDesignations.class,commonControllerUtility.getPageNumber(request));
		model.put("teacherdesignations",paginationObject.getRecords());
		commonControllerUtility.setCommonAttributesOfPagination(paginationObject, model, commonControllerUtility.getPageNumber(request));
		return new ModelAndView("teacherdesignations_list",model);
	}
	
	@RequestMapping(value = "/addteacherdesignation", method = RequestMethod.GET)
	public ModelAndView addTeacherDesignation(HttpServletRequest request,
			HttpServletResponse response, ModelMap model) {
		model.put("teacherdesignation", new TeacherDesignations());
		return new ModelAndView("teacherdesignation_form",model);
	}
	
	@RequestMapping(value = "/editteacherdesignation", method = RequestMethod.GET)
	public ModelAndView editTeacherDesignation(HttpServletRequest request,
			HttpServletResponse response, ModelMap model) {
		String id = RequestCommon.getAttributeValueFromRequest("id", request);
		if(id!=null && !id.equals(""))
			model.put("teacherdesignation", DBCommunicator.getApiServices().getGenericApi().fetchObjectbyID(TeacherDesignations.class, id));
		else
			model.put("teacherdesignation", new TeacherDesignations());
		return new ModelAndView("teacherdesignation_form",model);
	}

	@RequestMapping(value = "/addteacherdesignation", method = RequestMethod.POST)
	public ModelAndView submitAddTeacherDesignationForm(@Valid TeacherDesignations teacherDesignation, BindingResult bindingResult,
			HttpServletRequest request, HttpServletResponse response, ModelMap model) {
		return addEditTeacherDesignation(teacherDesignation, bindingResult, request, response, model,true);
	}
	
	private ModelAndView addEditTeacherDesignation(TeacherDesignations teacherDesignation, BindingResult bindingResult,
			HttpServletRequest request, HttpServletResponse response, ModelMap model,Boolean isAddingRecord){
		if(!bindingResult.hasErrors()){
			Boolean result = false;
			if(isAddingRecord)
				result = DBCommunicator.getApiServices().getGenericApi().insert(teacherDesignation);
			else result = DBCommunicator.getApiServices().getGenericApi().update(teacherDesignation);
			if(result){
				return new ModelAndView("redirect:/teacherdesignations.htm", model);
			}
		}
		model.put("teacherdesignation", teacherDesignation);
		ValidatorCommon.checkAndAddFieldErrors(bindingResult, model);
    	return new ModelAndView("teacherdesignation_form",model);
	}
	
	@RequestMapping(value = "/editteacherdesignation", method = RequestMethod.POST)
	public ModelAndView submitEditTeacherDesignationForm(@Valid TeacherDesignations teacherDesignations, BindingResult bindingResult,
			HttpServletRequest request, HttpServletResponse response, ModelMap model) {
		return addEditTeacherDesignation(teacherDesignations, bindingResult, request, response, model,false);
	}

	@RequestMapping(value = "/staffdesignations", method = RequestMethod.GET)
	public ModelAndView viewStaffdesignations(HttpServletRequest request,
			HttpServletResponse response, ModelMap model) {
		PaginationObject paginationObject = commonControllerUtility.getObjectsByClass(SupportingStaffDesignations.class,commonControllerUtility.getPageNumber(request));
		model.put("staffdesignations",paginationObject.getRecords());
		commonControllerUtility.setCommonAttributesOfPagination(paginationObject, model, commonControllerUtility.getPageNumber(request));
		return new ModelAndView("staffdesignations_list",model);
	}
	
	@RequestMapping(value = "/addstaffdesignation", method = RequestMethod.GET)
	public ModelAndView addStaffDesignation(HttpServletRequest request,
			HttpServletResponse response, ModelMap model) {
		model.put("staffdesignation", new SupportingStaffDesignations());
		return new ModelAndView("staffdesignation_form",model);
	}
	
	@RequestMapping(value = "/editstaffdesignation", method = RequestMethod.GET)
	public ModelAndView editStaffDesignation(HttpServletRequest request,
			HttpServletResponse response, ModelMap model) {
		String id = RequestCommon.getAttributeValueFromRequest("id", request);
		if(id!=null && !id.equals(""))
			model.put("staffdesignation", DBCommunicator.getApiServices().getGenericApi().fetchObjectbyID(SupportingStaffDesignations.class, id));
		else
			model.put("staffdesignation", new SupportingStaffDesignations());
		return new ModelAndView("staffdesignation_form",model);
	}

	@RequestMapping(value = "/addstaffdesignation", method = RequestMethod.POST)
	public ModelAndView submitAddStaffDesignationForm(@Valid SupportingStaffDesignations supportingStaffDesignations, BindingResult bindingResult,
			HttpServletRequest request, HttpServletResponse response, ModelMap model) {
		return addEditStaffDesignation(supportingStaffDesignations, bindingResult, request, response, model, true);
	}
	
	private ModelAndView addEditStaffDesignation(SupportingStaffDesignations supportingStaffDesignations, BindingResult bindingResult,
			HttpServletRequest request, HttpServletResponse response, ModelMap model,Boolean isAddingRecord){
		if(!bindingResult.hasErrors()){
			Boolean result = false;
			if(isAddingRecord)
				result = DBCommunicator.getApiServices().getGenericApi().insert(supportingStaffDesignations);
			else result = DBCommunicator.getApiServices().getGenericApi().update(supportingStaffDesignations);
			if(result){
				return new ModelAndView("redirect:/staffdesignations.htm", model);
			}
		}
		model.put("staffdesignation", supportingStaffDesignations);
		ValidatorCommon.checkAndAddFieldErrors(bindingResult, model);
    	return new ModelAndView("staffdesignation_form",model);
	}

	@RequestMapping(value = "/editstaffdesignation", method = RequestMethod.POST)
	public ModelAndView submitEditStaffDesignationForm(@Valid SupportingStaffDesignations supportingStaffDesignations, BindingResult bindingResult,
			HttpServletRequest request, HttpServletResponse response, ModelMap model) {
		return addEditStaffDesignation(supportingStaffDesignations, bindingResult, request, response, model, false);
	}
	
	@InitBinder
    protected void initBinder(WebDataBinder binder) {
    	binder.setValidator(settingsValidator);
    }
}