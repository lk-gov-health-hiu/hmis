package com.divudi.bean.common;

import com.divudi.data.RelationshipType;
import com.divudi.entity.Area;
import com.divudi.entity.Institution;
import com.divudi.entity.Item;
import com.divudi.entity.Relationship;
import com.divudi.facade.AreaFacade;
import com.divudi.facade.RelationshipFacade;
import com.divudi.facade.util.JsfUtil;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.ejb.EJB;
import javax.inject.Named;
import javax.enterprise.context.SessionScoped;
import javax.faces.component.UIComponent;
import javax.faces.context.FacesContext;
import javax.faces.convert.Converter;
import javax.faces.convert.FacesConverter;
import javax.inject.Inject;
import org.primefaces.model.UploadedFile;

@Named
@SessionScoped
public class RelationshipController implements Serializable {

    @EJB
    private RelationshipFacade ejbFacade;
    @EJB
    private AreaFacade areaFacade;

    @Inject
    private WebUserController webUserController;
    @Inject
    private AreaController areaController;

    @Inject
    InstitutionController institutionController;
    @Inject
    ItemController itemController;

    private List<Relationship> items = null;
    private Relationship selected;

    private RelationshipType rt;

    private Area area;
    private Institution institution;
    private Institution procedureRoom;
    Item procedure;

    private Integer year;
    private Integer month;
    private Long populationValue;

    private Relationship adding;
    private Relationship removing;

    private int districtColumnNumber;
    private int estimatedMidyearPopulationColumnNumber;
    private int targetPopulationColumnNumber;
    private int parentCodeColumnNumber;
    private int startRow = 1;

    private UploadedFile file;
    private String errorCode;

    public void fillAll() {
        items = getFacade().findAll();
    }

    public void save() {
        save(selected);
        JsfUtil.addSuccessMessage("Saved");
    }

    public void save(Relationship r) {
        if (r == null) {
            JsfUtil.addErrorMessage("Nothing selected");
            return;
        }
        if (r.getId() == null) {
            r.setCreatedAt(new Date());
            getFacade().create(r);
        } else {
            r.setLastEditeAt(new Date());
            getFacade().edit(r);
        }
    }

    public void fillDispensoriesForSelectedInstitution() {
        items = findRelationships(institution, RelationshipType.Procedure_Room);
        if (items == null) {
            items = new ArrayList<>();
        }
    }

    public void fillProceduresForSelectedProcedureRoom() {
        items = findRelationships(institution, RelationshipType.Procedure_for_institution);
        if (items == null) {
            items = new ArrayList<>();
        }
    }

    public void fillRelationshipData() {
        if (area == null) {
            return;
        }
        String j = "select r from Relationship r "
                + " where (r.area=:a or r.area.parentArea=:a or r.area.parentArea.parentArea=:a or r.area.parentArea.parentArea.parentArea=:a "
                + " or r.area.phm=:a or r.area.phi=:a or r.area.dsd=:a  or r.area.moh=:a  or  r.area.district=:a  or  r.area.province=:a  or r.area.rdhsArea=:a  or r.area.pdhsArea=:a)  "
                + " and r.retired=false "
                + " and r.yearInt=:y";
        j = "select r from Relationship r "
                + " where r.area=:a  "
                + " and r.retired=:ret "
                + " and r.yearInt=:y";

        Map m = new HashMap();
        m.put("a", area);
        m.put("y", getYear());
        m.put("ret", false);
        items = getFacade().findBySQL(j, m);
    }

    public String toFillAreaData() {
        items = null;
        return "/area/view_population_data";
    }

    public void saveAreaRelationshipDate() {
        if (area == null) {
            JsfUtil.addErrorMessage("Area ?");
            return;
        }
        if (populationValue == null) {
            JsfUtil.addErrorMessage("Population ?");
            return;
        }
        if (rt == null) {
            JsfUtil.addErrorMessage("Type ?");
            return;
        }
        Relationship r;
        r = findRelationship(year, area, rt);
        if (r == null) {
            r = new Relationship();
            r.setArea(area);
            r.setRelationshipType(rt);
            r.setYearInt(year);
            r.setLongValue1(populationValue);
            r.setCreatedAt(new Date());
            getFacade().create(r);
            JsfUtil.addSuccessMessage("Data Added");
        } else {
            r.setLongValue1(populationValue);
            r.setLastEditeAt(new Date());
            getFacade().edit(r);
            JsfUtil.addSuccessMessage("Data Updated");
        }
    }

    public void saveInstitutionRelationshipDate() {
        if (institution == null) {
            JsfUtil.addErrorMessage("Institution ?");
            return;
        }
        if (populationValue == null) {
            JsfUtil.addErrorMessage("Population ?");
            return;
        }
        if (rt == null) {
            JsfUtil.addErrorMessage("Type ?");
            return;
        }
        Relationship r;
        r = findRelationship(year, institution, rt);
        if (r == null) {
            r = new Relationship();
            r.setInstitution(institution);
            r.setRelationshipType(rt);
            r.setYearInt(year);
            r.setLongValue1(populationValue);
            r.setCreatedAt(new Date());
            getFacade().create(r);
            JsfUtil.addSuccessMessage("Data Added");
        } else {
            r.setLongValue1(populationValue);
            r.setLastEditeAt(new Date());
            getFacade().edit(r);
            JsfUtil.addSuccessMessage("Data Updated");
        }
    }


    public Relationship findRelationship(int y, Institution ins, RelationshipType t) {
        String j = "select r from Relationship r "
                + " where r.institution=:ins   "
                + " and r.relationshipType=:rt "
                + " and r.yearInt=:y";
        Map m = new HashMap();
        m.put("ins", ins);
        m.put("y", y);
        m.put("rt", t);
        return getFacade().findFirstBySQL(j, m);
    }

    public Relationship findRelationship(Institution ins, Institution toIns, RelationshipType t) {
        String j = "select r from Relationship r "
                + " where r.retired=:r "
                + " and r.institution=:i   "
                + " and r.toInstitution=:t "
                + " and r.relationshipType=:rt ";
        Map m = new HashMap();
        m.put("r", false);
        m.put("i", ins);
        m.put("t", toIns);
        m.put("rt", t);
        return getFacade().findFirstBySQL(j, m);
    }

    public Relationship findRelationship(Institution ins, Item item, RelationshipType t) {
        String j = "select r from Relationship r "
                + " where r.retired=:r "
                + " and r.institution=:i   "
                + " and r.item=:t "
                + " and r.relationshipType=:rt ";
        Map m = new HashMap();
        m.put("r", false);
        m.put("i", ins);
        m.put("t", item);
        m.put("rt", t);
        return getFacade().findFirstBySQL(j, m);
    }

    public Relationship findRelationship(Item item, Item itemUnit, Item toItem, Double dblValue,
            Item toItemUnit, RelationshipType t) {
        String j = "select r from Relationship r "
                + " where r.retired=:r "
                + " and r.item=:item   "
                + " and r.itemUnit=:itemUnit "
                + " and r.toItem=:toItem "
                + " and r.toItemUnit=:toItemUnit "
                + " and r.dblValue=:dblValue "
                + " and r.relationshipType=:rt ";
        Map m = new HashMap();
        m.put("r", false);
        m.put("item", item);
        m.put("itemUnit", itemUnit);

        m.put("toItem", toItem);
        m.put("toItemUnit", toItemUnit);
        m.put("dblValue", dblValue);

        m.put("rt", t);

        return getFacade().findFirstBySQL(j, m);
    }

    public Relationship findRelationship(Item item, Item toItem, RelationshipType t) {
        String j = "select r from Relationship r "
                + " where r.retired=:r "
                + " and r.item=:item   "
                + " and r.toItem=:toItem "
                + " and r.relationshipType=:rt ";
        Map m = new HashMap();
        m.put("r", false);
        m.put("item", item);
        m.put("toItem", toItem);
        m.put("rt", t);
        return getFacade().findFirstBySQL(j, m);
    }

    public Relationship findRelationship(int y, Area area, RelationshipType t) {
        String j = "select r from Relationship r "
                + " where r.area=:area   "
                + " and r.relationshipType=:rt "
                + " and r.yearInt=:y";
        Map m = new HashMap();
        m.put("area", area);
        m.put("y", y);
        m.put("rt", t);
        return getFacade().findFirstBySQL(j, m);
    }

    public List<Relationship> findRelationships(Institution ins, RelationshipType t) {
        String j = "select r from Relationship r "
                + " where r.retired=:ret "
                + " and r.institution=:ins   "
                + " and r.relationshipType=:rt";
        Map m = new HashMap();
        m.put("ret", false);
        m.put("ins", ins);
        m.put("rt", t);
        return getFacade().findBySQL(j, m);
    }

    public Relationship findRelationship(Area a, RelationshipType type, Integer year) {
        return findRelationship(a, type, year, false);
    }

    public Relationship findRelationship(Area relArea, RelationshipType relType, Integer relYear, boolean create) {
        String j = "select r from Relationship r "
                + " where r.area.id=:a "
                + " and r.relationshipType=:t "
                + " and r.retired=:f ";
        Map m = new HashMap();
        m.put("f", false);
        if (relYear != null && relYear != 0) {
            j += " and r.yearInt=:y";
            m.put("y", relYear);
        }
        m.put("a", relArea.getId());
        m.put("t", relType);
        j += " order by r.id desc";

        Relationship r = getFacade().findFirstBySQL(j, m);
        if (r == null && create) {
            r = new Relationship();
            r.setArea(relArea);
            r.setRelationshipType(relType);
            r.setYearInt(relYear);
            getFacade().create(r);
        }
        return r;
    }

    public Relationship findRelationship(Area a, RelationshipType type) {
        return findRelationship(a, type, 0);
    }

    public RelationshipController() {
    }

    public Relationship getSelected() {
        return selected;
    }

    public void setSelected(Relationship selected) {
        this.selected = selected;
    }

    protected void setEmbeddableKeys() {
    }

    protected void initializeEmbeddableKey() {
    }

    private RelationshipFacade getFacade() {
        return ejbFacade;
    }

    public Relationship prepareCreate() {
        selected = new Relationship();
        initializeEmbeddableKey();
        return selected;
    }

   
    public List<Relationship> getItems() {
        return items;
    }

    public Relationship getRelationship(java.lang.Long id) {
        return getFacade().find(id);
    }

    public List<Relationship> getItemsAvailableSelectMany() {
        return getFacade().findAll();
    }

    public List<Relationship> getItemsAvailableSelectOne() {
        return getFacade().findAll();
    }

    public WebUserController getWebUserController() {
        return webUserController;
    }

    public RelationshipFacade getEjbFacade() {
        return ejbFacade;
    }

    public Area getArea() {
        return area;
    }

    public void setArea(Area area) {
        this.area = area;
    }

    public Relationship getAdding() {
        if (adding == null) {
            adding = new Relationship();
        }
        return adding;
    }

    public void setAdding(Relationship adding) {
        this.adding = adding;
    }

    public Relationship getRemoving() {
        return removing;
    }

    public void setRemoving(Relationship removing) {
        this.removing = removing;
    }

    public Integer getYear() {
        if (year == null || year == 0) {
            Calendar c = Calendar.getInstance();
            year = c.get(Calendar.YEAR);
        }
        return year;
    }

    public void setYear(Integer year) {
        this.year = year;
    }

    public int getMonth() {
        return month;
    }

    public void setMonth(int month) {
        this.month = month;
    }

    public int getDistrictColumnNumber() {
        return districtColumnNumber;
    }

    public void setDistrictColumnNumber(int districtColumnNumber) {
        this.districtColumnNumber = districtColumnNumber;
    }

    public int getEstimatedMidyearPopulationColumnNumber() {
        return estimatedMidyearPopulationColumnNumber;
    }

    public void setEstimatedMidyearPopulationColumnNumber(int estimatedMidyearPopulationColumnNumber) {
        this.estimatedMidyearPopulationColumnNumber = estimatedMidyearPopulationColumnNumber;
    }

    public int getTargetPopulationColumnNumber() {
        return targetPopulationColumnNumber;
    }

    public void setTargetPopulationColumnNumber(int targetPopulationColumnNumber) {
        this.targetPopulationColumnNumber = targetPopulationColumnNumber;
    }

    public int getParentCodeColumnNumber() {
        return parentCodeColumnNumber;
    }

    public void setParentCodeColumnNumber(int parentCodeColumnNumber) {
        this.parentCodeColumnNumber = parentCodeColumnNumber;
    }

    public int getStartRow() {
        return startRow;
    }

    public void setStartRow(int startRow) {
        this.startRow = startRow;
    }

    public UploadedFile getFile() {
        return file;
    }

    public void setFile(UploadedFile file) {
        this.file = file;
    }

    public AreaController getAreaController() {
        return areaController;
    }

    public AreaFacade getAreaFacade() {
        return areaFacade;
    }

   
    public RelationshipType getRt() {
        return rt;
    }

    public void setRt(RelationshipType rt) {
        this.rt = rt;
    }

    public Institution getInstitution() {
        return institution;
    }

    public void setInstitution(Institution institution) {
        this.institution = institution;
    }

    public Long getPopulationValue() {
        return populationValue;
    }

    public void setPopulationValue(Long populationValue) {
        this.populationValue = populationValue;
    }

    public Institution getProcedureRoom() {
        return procedureRoom;
    }

    public void setProcedureRoom(Institution procedureRoom) {
        this.procedureRoom = procedureRoom;
    }

    public Item getProcedure() {
        return procedure;
    }

    public void setProcedure(Item procedure) {
        this.procedure = procedure;
    }

   
    @FacesConverter(forClass = Relationship.class)
    public static class RelationshipControllerConverter implements Converter {

        @Override
        public Object getAsObject(FacesContext facesContext, UIComponent component, String value) {
            if (value == null || value.length() == 0) {
                return null;
            }
            RelationshipController controller = (RelationshipController) facesContext.getApplication().getELResolver().
                    getValue(facesContext.getELContext(), null, "relationshipController");
            return controller.getRelationship(getKey(value));
        }

        java.lang.Long getKey(String value) {
            java.lang.Long key;
            key = Long.valueOf(value);
            return key;
        }

        String getStringKey(java.lang.Long value) {
            StringBuilder sb = new StringBuilder();
            sb.append(value);
            return sb.toString();
        }

        @Override
        public String getAsString(FacesContext facesContext, UIComponent component, Object object) {
            if (object == null) {
                return null;
            }
            if (object instanceof Relationship) {
                Relationship o = (Relationship) object;
                return getStringKey(o.getId());
            } else {
                Logger.getLogger(this.getClass().getName()).log(Level.SEVERE, "object {0} is of type {1}; expected type: {2}", new Object[]{object, object.getClass().getName(), Relationship.class.getName()});
                return null;
            }
        }

    }

}
