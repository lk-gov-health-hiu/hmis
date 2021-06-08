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
import com.divudi.entity.Department;
import com.divudi.entity.Item;
import com.divudi.entity.pharmacy.Amp;
import com.divudi.entity.pharmacy.MeasurementUnit;
import com.divudi.entity.pharmacy.PharmaceuticalItemCategory;
import com.divudi.entity.pharmacy.PharmaceuticalItemType;
import com.divudi.entity.pharmacy.Vmp;
import com.divudi.entity.pharmacy.Vtm;
import com.divudi.facade.DepartmentFacade;
import java.math.BigInteger;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import javax.ejb.EJB;
import javax.inject.Named;
import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;
import org.json.JSONArray;
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

    public void fillMedicineAndUnits() {
        NewJerseyClient newJerseyClient = new NewJerseyClient();
        response = newJerseyClient.getJson("", "", "get_medicines_and_units_list", "", "");
        newJerseyClient.close();
        JSONObject jSONObjectOut = new JSONObject(response);
        JSONArray array = jSONObjectOut.getJSONArray("data");
        for (Object o : array) {
            if (o instanceof JSONObject) {
                JSONObject jo = ((JSONObject) o);
                BigInteger id = jo.getBigInteger("item_id");
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
                        vtmController.save(vtm);
                        break;
                    case "Amp":
                        Amp amp = ampController.getByCoreAppId(coreId);
                        if (amp == null) {
                            amp = new Amp();
                            amp.setCoreAppId(coreId);
                        }
                        amp.setName(name);
                        amp.setCode(code);
                        amp.setStrType(type);
                        ampController.save(amp);
                        break;
                    case "Vmp":
                        Vmp vmp = vmpController.getByCoreAppId(coreId);
                        if (vmp == null) {
                            vmp = new Vmp();
                            vmp.setCoreAppId(coreId);
                        }
                        vmp.setName(name);
                        vmp.setCode(code);
                        vmp.setStrType(type);
                        vmpController.save(vmp);
                        break;
                    case "Ampp":
                    case "Vmpp":
                        break;
                    case "Dosage_Form":
                        PharmaceuticalItemCategory pd;
                        pd = pharmaceuticalItemCategoryController.findCategory(coreId);
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
                        MeasurementUnit u = measurementUnitController.getByCoreAppId(coreId);
                        if(u==null){
                            u = new MeasurementUnit();
                            u.setCoreAppId(coreId);
                        }
                        u.setName(name);
                        u.setCode(code);
                        u.setStrType(type);
                        measurementUnitController.save(u);
                        break;
                }
            }
        }
    }

    public void executeMedicineUnitListRestRequest() {
        NewJerseyClient newJerseyClient = new NewJerseyClient();
        response = newJerseyClient.getJson("", "", "get_medicine_units", "", "");
        newJerseyClient.close();
        JSONObject jSONObjectOut = new JSONObject(response);
        JSONArray array = jSONObjectOut.getJSONArray("data");
        for (Object o : array) {
            if (o instanceof JSONObject) {
                JSONObject jo = ((JSONObject) o);

                BigInteger id = jo.getBigInteger("item_id");

                String name = jo.getString("item_name");
                String hin = jo.getString("hin");
                String code = jo.getString("item_code");
                String type = jo.getString("item_type");

                Long coreId;
                try {
                    coreId = id.longValue();
                } catch (Exception e) {
                    System.out.println("e = " + e);
                    continue;
                }
                Item d;

                String j = "select i "
                        + " from Item i "
                        + " where i.coreAppId=:cid";
                Map m = new HashMap();
                m.put("cid", coreId);

//                d = facade.findFirstBySQL(j, m);
//                if (d == null) {
//                    d = new Department();
//                    d.setCoreAppId(coreId);
//                }
//                d.setName(name);
//                d.setCode(code);
//                d.setCoreAppId(coreId);
//                d.setStrType(type);
//                d.setInstitution(sessionController.getLoggedUser().getInstitution());
//                if (d.getId() == null) {
//                    facade.create(d);
//                } else {
//                    facade.edit(d);
//                }
            }
        }
    }

    public String getResponse() {
        return response;
    }

    public void setResponse(String response) {
        this.response = response;
    }

}
