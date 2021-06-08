/*
 * MSc(Biomedical Informatics) Project
 *
 * Development and Implementation of a Web-based Combined Data Repository of
 Genealogical, Clinical, Laboratory and Genetic Data
 * and
 * a Set of Related Tools
 */
package com.divudi.bean.pharmacy;
import com.divudi.bean.common.SessionController;
import com.divudi.bean.common.UtilityController;
import com.divudi.entity.pharmacy.Amp;
import com.divudi.entity.pharmacy.MeasurementUnit;
import com.divudi.facade.MeasurementUnitFacade;
import java.io.Serializable;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import javax.ejb.EJB;
import javax.enterprise.context.SessionScoped;
import javax.faces.component.UIComponent;
import javax.faces.context.FacesContext; import javax.faces.convert.Converter;
import javax.faces.convert.FacesConverter;
import javax.inject.Inject;
import javax.inject.Named;

/**
 *
 * @author Dr. M. H. B. Ariyaratne, MBBS, PGIM Trainee for MSc(Biomedical
 Informatics)
 */
@Named
@SessionScoped
public  class MeasurementUnitController implements Serializable {

    private static final long serialVersionUID = 1L;
    @Inject
    SessionController sessionController;
    @EJB
    private MeasurementUnitFacade ejbFacade;
    List<MeasurementUnit> selectedItems;
    private MeasurementUnit current;
    private List<MeasurementUnit> items = null;
    String selectText = "";


    public MeasurementUnit getByCoreAppId(Long id) {
        String sql;
        sql = "select c "
                + " from MeasurementUnit c "
                + " where c.retired=false "
                + " and c.coreAppId=:id";
        Map m = new HashMap();
        m.put("id", id);
        return getFacade().findFirstBySQL(sql, m);
    }

    public void save(MeasurementUnit v) {
        if (v == null) {
            System.out.println("Nothing to save");
        }
        if (v.getId() == null) {
            getFacade().create(v);
        } else {
            getFacade().create(v);
        }
    }

    
    public void prepareAdd() {
        current = new MeasurementUnit();
    }

    public void setSelectedItems(List<MeasurementUnit> selectedItems) {
        this.selectedItems = selectedItems;
    }

    public String getSelectText() {
        return selectText;
    }

    private void recreateModel() {
        items = null;
    }

    public void saveSelected() {

        if (getCurrent().getId() != null && getCurrent().getId() > 0) {
            getFacade().edit(current);
            UtilityController.addSuccessMessage("Updated Successfully.");
        } else {
            current.setCreatedAt(new Date());
            current.setCreater(getSessionController().getLoggedUser());
            getFacade().create(current);
            UtilityController.addSuccessMessage("Saved Successfully");
        }
        recreateModel();
        getItems();
    }

    public void setSelectText(String selectText) {
        this.selectText = selectText;
    }

    public MeasurementUnitFacade getEjbFacade() {
        return ejbFacade;
    }

    public void setEjbFacade(MeasurementUnitFacade ejbFacade) {
        this.ejbFacade = ejbFacade;
    }

    public SessionController getSessionController() {
        return sessionController;
    }

    public void setSessionController(SessionController sessionController) {
        this.sessionController = sessionController;
    }

    public MeasurementUnitController() {
    }

    public MeasurementUnit getCurrent() {
        return current;
    }

    public void setCurrent(MeasurementUnit current) {
        this.current = current;
    }

    public void delete() {

        if (current != null) {
            current.setRetired(true);
            current.setRetiredAt(new Date());
            current.setRetirer(getSessionController().getLoggedUser());
            getFacade().edit(current);
            UtilityController.addSuccessMessage("Deleted Successfully");
        } else {
            UtilityController.addSuccessMessage("Nothing to Delete");
        }
        recreateModel();
        getItems();
        current = null;
        getCurrent();
    }

    private MeasurementUnitFacade getFacade() {
        return ejbFacade;
    }

    public List<MeasurementUnit> getItems() {
        items = getFacade().findAll("name", true);
        return items;
    }

    /**
     *
     */
    @FacesConverter(forClass = MeasurementUnit.class)
    public static class MeasurementUnitControllerConverter implements Converter {

        @Override
        public Object getAsObject(FacesContext facesContext, UIComponent component, String value) {
            if (value == null || value.length() == 0) {
                return null;
            }
            MeasurementUnitController controller = (MeasurementUnitController) facesContext.getApplication().getELResolver().
                    getValue(facesContext.getELContext(), null, "measurementUnitController");
            return controller.getEjbFacade().find(getKey(value));
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
            if (object instanceof MeasurementUnit) {
                MeasurementUnit o = (MeasurementUnit) object;
                return getStringKey(o.getId());
            } else {
                throw new IllegalArgumentException("object " + object + " is of type "
                        + object.getClass().getName() + "; expected type: " + MeasurementUnitController.class.getName());
            }
        }
    }
}
