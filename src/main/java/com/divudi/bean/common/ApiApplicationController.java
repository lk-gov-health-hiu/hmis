/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.divudi.bean.common;

import com.divudi.bean.pharmacy.AmpController;
import com.divudi.bean.pharmacy.MeasurementUnitController;
import com.divudi.bean.pharmacy.PharmaceuticalItemCategoryController;
import com.divudi.bean.pharmacy.PharmaceuticalItemTypeController;
import com.divudi.bean.pharmacy.VmpController;
import com.divudi.bean.pharmacy.VtmController;
import com.divudi.bean.rest.NewJerseyClient;
import com.divudi.data.BillType;
import com.divudi.data.DepartmentType;
import lk.gov.health.phsp.pojocs.PrescriptionItemPojo;
import com.divudi.ejb.PharmacyBean;
import com.divudi.entity.Bill;
import com.divudi.entity.BillItem;
import com.divudi.entity.Department;
import com.divudi.entity.Item;
import com.divudi.entity.Patient;
import com.divudi.entity.Person;
import com.divudi.entity.PreBill;
import com.divudi.entity.pharmacy.Amp;
import com.divudi.entity.pharmacy.MeasurementUnit;
import com.divudi.entity.pharmacy.PharmaceuticalBillItem;
import com.divudi.entity.pharmacy.PharmaceuticalItem;
import com.divudi.entity.pharmacy.PharmaceuticalItemCategory;
import com.divudi.entity.pharmacy.PharmaceuticalItemType;
import com.divudi.entity.pharmacy.Vmp;
import com.divudi.entity.pharmacy.Vtm;
import com.divudi.facade.DepartmentFacade;
import com.divudi.facade.PharmaceuticalBillItemFacade;
import java.math.BigInteger;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import javax.ejb.EJB;
import javax.inject.Named;
import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;
import lk.gov.health.phsp.pojocs.PrescriptionPojo;
import org.apache.commons.collections4.list.PredicatedList;
import org.eclipse.persistence.descriptors.invalidation.DailyCacheInvalidationPolicy;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

/**
 *
 * @author buddhika
 */
@Named
@ApplicationScoped
public class ApiApplicationController {

    @EJB
    DepartmentFacade facade;
    @EJB
    PharmacyBean pharmacyBean;
    @EJB
    PharmaceuticalBillItemFacade pharmaceuticalBillItemFacade;

    @Inject
    WebUserController webUserController;
    @Inject
    SessionController sessionController;
    @Inject
    PharmaceuticalItemCategoryController pharmaceuticalItemCategoryController;
    @Inject
    PharmaceuticalItemTypeController pharmaceuticalItemTypeController;
    @Inject
    VtmController vtmController;
    @Inject
    VmpController vmpController;
    @Inject
    AmpController ampController;
    @Inject
    MeasurementUnitController measurementUnitController;
    @Inject
    DepartmentController departmentController;
    @Inject
    ItemController itemController;
    @Inject
    CategoryController categoryController;
    @Inject
    BillController billController;
    @Inject
    BillItemController billItemController;
    @Inject
    PersonController personController;
    @Inject
    PatientController patientController;

    private String response;

    /**
     * Creates a new instance of InstitutionApplicationController
     */
    public ApiApplicationController() {
    }

    public void fillDepartments() {
        NewJerseyClient newJerseyClient = new NewJerseyClient();
        response = newJerseyClient.getJson("", "", "get_institute_list", "", "");
        newJerseyClient.close();
        JSONObject jSONObjectOut = new JSONObject(response);
        JSONArray array = jSONObjectOut.getJSONArray("data");
        for (Object o : array) {
            if (o instanceof JSONObject) {
                JSONObject jo = ((JSONObject) o);
                String name = jo.getString("name");
                String hin = jo.getString("hin");
                BigInteger id = jo.getBigInteger("institute_id");
                String code = jo.getString("institute_code");
                String type = jo.getString("type");
                Long coreId;
                try {
                    coreId = id.longValue();
                } catch (Exception e) {
                    System.out.println("e = " + e);
                    continue;
                }
                Department d;

                String j = "select i "
                        + " from Department i "
                        + " where i.coreAppId=:cid";
                Map m = new HashMap();
                m.put("cid", coreId);

                d = facade.findFirstBySQL(j, m);
                if (d == null) {
                    d = new Department();
                    d.setCoreAppId(coreId);
                }
                d.setName(name);
                d.setCode(code);
                d.setCoreAppId(coreId);
                d.setStrType(type);
                d.setInstitution(sessionController.getLoggedUser().getInstitution());
                if (d.getId() == null) {
                    facade.create(d);
                } else {
                    facade.edit(d);
                }
            }
        }
    }

    public void fillMedicineData() {
        NewJerseyClient newJerseyClient = new NewJerseyClient();
        response = newJerseyClient.getJson("", "", "get_medicines_and_units_list", "", "");
        newJerseyClient.close();
        JSONObject jSONObjectOut = new JSONObject(response);
        JSONArray array = jSONObjectOut.getJSONArray("data");
        for (Object o : array) {
            if (o instanceof JSONObject) {
                JSONObject jo = ((JSONObject) o);
                BigInteger id = jo.getBigInteger("item_id");
                BigInteger parent_id = jo.getBigInteger("parent_id");
                String name = jo.getString("item_name");
                String code = jo.getString("item_code");
                String type = jo.getString("item_type");

                Long coreId;
                try {
                    coreId = id.longValue();
                } catch (Exception e) {
                    System.err.println("e = " + e);
                    continue;
                }
                Long coreParentId;
                try {
                    coreParentId = parent_id.longValue();
                } catch (Exception e) {
                    coreParentId = 0l;
                    System.err.println("e = " + e);
                }
                Item d;
                if (type == null) {
                    System.err.println("No Type");
                    continue;
                }
                switch (type) {
                    case "Atm":
                        //TODO: Add Code
                        break;
                    case "Vtm":
                        Vtm vtm = vtmController.getByCoreAppId(coreId);
                        if (vtm == null) {
                            vtm = new Vtm();
                            vtm.setCoreAppId(coreId);
                        }
                        vtm.setName(name);
                        vtm.setCode(code);
                        vtm.setStrType(type);
                        vtm.setDepartmentType(DepartmentType.Pharmacy);
                        vtmController.save(vtm);
                        break;
                    case "Amp":
                        PharmaceuticalItemCategory catAmp = pharmaceuticalItemCategoryController.getByCoreAppId(coreParentId);
                        Amp amp = ampController.getByCoreAppId(coreId);
                        if (amp == null) {
                            amp = new Amp();
                            amp.setCoreAppId(coreId);
                        }
                        amp.setName(name);
                        amp.setDepartmentType(DepartmentType.Pharmacy);
                        amp.setCode(code);
                        amp.setStrType(type);
                        amp.setCategory(catAmp);
                        ampController.save(amp);
                        break;
                    case "Vmp":
                        Vmp vmp = vmpController.getByCoreAppId(coreId);
                        PharmaceuticalItemCategory catVmp = pharmaceuticalItemCategoryController.getByCoreAppId(coreParentId);
                        if (vmp == null) {
                            vmp = new Vmp();
                            vmp.setCoreAppId(coreId);
                        }
                        vmp.setName(name);
                        vmp.setCode(code);
                        vmp.setStrType(type);
                        vmp.setDepartmentType(DepartmentType.Pharmacy);
                        vmp.setCategory(catVmp);
                        vmpController.save(vmp);
                        break;
                    case "Ampp":
                    case "Vmpp":
                        break;
                    case "Dosage_Form":
                        PharmaceuticalItemCategory pd;
                        pd = pharmaceuticalItemCategoryController.getByCoreAppId(coreId);
                        if (pd == null) {
                            pd = new PharmaceuticalItemCategory();
                            pd.setCoreAppId(coreId);
                        }
                        pd.setName(name);
                        pd.setCode(code);
                        pd.setStrType(type);
                        pharmaceuticalItemCategoryController.save(pd);

                        PharmaceuticalItemType p = pharmaceuticalItemTypeController.getItemByCoreAppId(coreId);
                        if (p == null) {
                            p = new PharmaceuticalItemType();
                            p.setCoreAppId(coreId);
                        }
                        p.setName(name);
                        p.setCode(code);
                        p.setStrType(type);
                        pharmaceuticalItemTypeController.save(p);
                        break;
                    case "Strength_Unit":
                    case "Issue_Unit":
                    case "Pack_Unit":
                    case "Frequency_Unit":
                    case "Duration_Unit":
                        MeasurementUnit mu = measurementUnitController.getByCoreAppId(coreId);
                        if (mu == null) {
                            mu = new MeasurementUnit();
                            mu.setCoreAppId(coreId);
                        }
                        mu.setName(name);
                        mu.setCode(code);
                        mu.setStrType(type);
                        measurementUnitController.save(mu);
                        break;
                }
            }
        }
    }

    public void fillMedicineRelationships() {
        NewJerseyClient newJerseyClient = new NewJerseyClient();
        response = newJerseyClient.getJson("", "", "get_medicine_relationships", "", "");
        newJerseyClient.close();
        JSONObject jSONObjectOut = new JSONObject(response);
        JSONArray array = jSONObjectOut.getJSONArray("data");
        for (Object o : array) {
            if (o instanceof JSONObject) {
                BigInteger id;
                BigInteger itemId;
                BigInteger itemUnitId;
                BigInteger toItemId;
                BigInteger toItemUnitId;
                Double dblValue;
                JSONObject jo = ((JSONObject) o);
                try {
                    id = jo.getBigInteger("id");
                } catch (JSONException e) {
                    System.out.println("e in ID= " + e);
                    id = new BigInteger("0");
                }

                try {
                    dblValue = jo.getDouble("dbl_value");
                } catch (JSONException e) {
                    System.out.println("e in dblValue= " + e);
                    dblValue = 0.0;
                }

                try {
                    toItemUnitId = jo.getBigInteger("to_item_unit_id");
                } catch (JSONException e) {
                    System.out.println("e in toItemUnitId= " + e);
                    toItemUnitId = new BigInteger("0");
                }

                try {
                    toItemId = jo.getBigInteger("to_item_id");
                } catch (JSONException e) {
                    System.out.println("e in to_Item_ID= " + e);
                    toItemId = new BigInteger("0");
                }

                try {
                    itemUnitId = jo.getBigInteger("item_unit_id");
                } catch (JSONException e) {
                    System.out.println("e in Item Unit ID= " + e);
                    itemUnitId = new BigInteger("0");
                }

                try {
                    itemId = jo.getBigInteger("item_id");
                } catch (JSONException e) {
                    itemId = new BigInteger("0");
                    System.out.println("e in Item_ID= " + e);
                }

                String type = jo.getString("type");

                Long coreItemId;
                Long coreItemUnitId;
                Long coreToItemId;
                Long coreToItemUnitId;

                try {
                    coreItemId = itemId.longValue();
                    coreItemUnitId = itemUnitId.longValue();
                    coreToItemId = toItemId.longValue();
                    coreToItemUnitId = toItemUnitId.longValue();
                } catch (Exception e) {
                    System.err.println("e = " + e);
                    continue;
                }
                Item d;
                if (type == null) {
                    System.err.println("No Type");
                    continue;
                }
                switch (type) {
                    case "VmpForAmp":
                        Amp amp = ampController.getByCoreAppId(coreItemId);
                        if (amp == null) {
                            continue;
                        }
                        Vmp vmp = vmpController.getByCoreAppId(coreToItemId);
                        vmp.setDepartmentType(DepartmentType.Pharmacy);
                        amp.setVmp(vmp);
                        ampController.save(amp);
                        break;
                    case "VtmsForVmp":
                        Vmp vmp1 = vmpController.getByCoreAppId(coreItemId);
                        MeasurementUnit issueUnit = measurementUnitController.getByCoreAppId(coreItemUnitId);
                        Vtm vtm = vtmController.getByCoreAppId(coreToItemId);
                        MeasurementUnit strengthUnit = measurementUnitController.getByCoreAppId(coreToItemUnitId);

                        pharmacyBean.createVtmsForVmp(vmp1, vtm, dblValue, strengthUnit, issueUnit);

                        vtmController.save(vtm);
                        break;
                }
            }
        }
    }

    public void fillPendingPrescriptions() {
        NewJerseyClient newJerseyClient = new NewJerseyClient();
        response = newJerseyClient.getJson("", "", "get_prescriptions_pending", "", "");
        newJerseyClient.close();
        JSONObject jSONObjectOut = new JSONObject(response);
        JSONArray array = jSONObjectOut.getJSONArray("data");
        List<PrescriptionPojo> ps = new ArrayList<>();
        
        for (Object o : array) {
            if (o instanceof PrescriptionPojo) {
                PrescriptionPojo p = (PrescriptionPojo) o;
                PreBill b = new PreBill();
                b.setBillType(BillType.PharmacyPrescription);
                b.setFromDepartment(departmentController.getByCoreAppId(p.getInstitutionId()));
                b.setBillDate(new Date());
                b.setCreatedAt(new Date());
                Person person = new Person();
                person.setName(p.getName());
                Calendar cdob = Calendar.getInstance();
                cdob.add(Calendar.DATE, -p.getAgeInDays().intValue());
                person.setDob(cdob.getTime());
                personController.save(person);
                Patient pt = new Patient();
                patientController.save(pt);
                pt.setPerson(person);
                b.setPatient(pt);
                billController.save(b);
                for(PrescriptionItemPojo i:p.getItems()){
                    BillItem bi = new BillItem();
                    bi.setBill(b);
                    bi.setItem(itemController.getByCoreAppId(i.getMedicineId()));
                    PharmaceuticalBillItem pbi = new PharmaceuticalBillItem();
                    pbi.setDose(i.getDose());
                    pbi.setDoseUnit(measurementUnitController.getByCoreAppId(i.getDoseUnitId()));
                    pbi.setFrequencyUnit(measurementUnitController.getByCoreAppId(i.getFrequencyUnitId()));
                    pbi.setDuration(i.getDuration());
                    pbi.setDurationUnit(measurementUnitController.getByCoreAppId(i.getDurationUnitId()));
                    pbi.setIssueQty(i.getIssueQty());
                    pbi.setIssueUnit(measurementUnitController.getByCoreAppId(i.getIssueUnitId()));
                    getPharmaceuticalBillItemFacade().create(pbi);
                    bi.setPharmaceuticalBillItem(pbi);
                    billItemController.save(bi);
                }
                billController.save(b);
            }
            
        }
    }

    
    public String getResponse() {
        return response;
    }

    public void setResponse(String response) {
        this.response = response;
    }

    public PharmaceuticalBillItemFacade getPharmaceuticalBillItemFacade() {
        return pharmaceuticalBillItemFacade;
    }
    

}
