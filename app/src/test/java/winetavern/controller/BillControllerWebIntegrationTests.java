package winetavern.controller;

import org.javamoney.moneta.Money;
import org.junit.Before;
import org.junit.Test;
import org.salespointframework.useraccount.UserAccountManager;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.web.servlet.RequestBuilder;
import winetavern.AbstractWebIntegrationTests;
import winetavern.Helper;
import winetavern.model.accountancy.Bill;
import winetavern.model.accountancy.BillItem;
import winetavern.model.accountancy.BillItemRepository;
import winetavern.model.accountancy.BillRepository;
import winetavern.model.menu.DayMenuItem;
import winetavern.model.menu.DayMenuItemRepository;
import winetavern.model.user.EmployeeManager;
import winetavern.model.user.Roles;

import javax.transaction.Transactional;

import java.util.List;

import static org.hamcrest.core.Is.is;
import static org.junit.Assert.assertThat;
import static org.salespointframework.core.Currencies.EURO;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.user;
import static org.springframework.security.test.web.servlet.response.SecurityMockMvcResultMatchers.authenticated;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.model;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.view;

/**
 * @author Niklas Wünsche
 */

@Transactional
public class BillControllerWebIntegrationTests extends AbstractWebIntegrationTests {

    @Autowired private BillRepository billRepository;
    @Autowired private DayMenuItemRepository dayMenuItemRepository;
    @Autowired private BillItemRepository billItemRepository;
    @Autowired private EmployeeManager employeeManager;
    @Autowired private UserAccountManager userAccountManager;

    @Before
    public void before() {

    }

    @Test
    public void serviceAndAdminAuthorized() throws Exception {
        RequestBuilder serviceRequest = post("/service/bills")
                .with(user("service").roles(Roles.SERVICE.getRealNameOfRole()));

        mvc.perform(serviceRequest)
                .andExpect(authenticated());

        RequestBuilder adminRequest = post("/service/bills")
                .with(user("admin").roles(Roles.ADMIN.getRealNameOfRole()));

        mvc.perform(adminRequest)
                .andExpect(authenticated());
    }

    @Test
    public void showBillsModelAttributesRight() throws Exception{
        RequestBuilder request = post("/service/bills")
                .with(user("service").roles(Roles.SERVICE.getRealNameOfRole()));

        mvc.perform(request)
                .andExpect(model().attributeExists("active"))
                .andExpect(view().name("bills"));
    }

    @Test
    public void addBillRight() throws Exception {
        String desk = "Table 1";

        RequestBuilder request = post("/service/bills/add")
                .with(user("admin").roles(Roles.ADMIN.getRealNameOfRole()))
                .param("table", desk);

        mvc.perform(request)
                .andExpect(status().is3xxRedirection());

        Bill firstBill = Helper.getFirstItem(billRepository.findAll());

        assertThat(firstBill.getDesk(), is(desk));
    }

    @Test
    public void splitBillRight() throws Exception {
        // TODO Was passiert, wenn es BillItem bereits gibt?
        DayMenuItem dayMenuItem = new DayMenuItem("Product", "Description", Money.of(3, EURO), 3.0);
        dayMenuItemRepository.save(dayMenuItem);
        BillItem billItem = new BillItem(dayMenuItem);
        //billItemRepository.save(new BillItem(dayMenuItem));
        billItemRepository.save(billItem);
        Bill bill = new Bill("Table 1",
                employeeManager.findByUserAccount(userAccountManager.findByUsername("admin").get()).get());

        bill.changeItem(billItem, 3);
        billRepository.save(bill);

        RequestBuilder request = post("/service/bills/details/" + bill.getId() + "/split")
                .with(user("admin").roles(Roles.ADMIN.getRealNameOfRole()))
                .param("query", "1,2|");

        mvc.perform(request)
                .andExpect(model().attributeExists("leftbill"))
                .andExpect(view().name("splitbill"));

        assertThat(Helper.getFirstItem(bill.getItems()).getQuantity(), is(1));
        Bill secondBill = Helper.convertToArray(billRepository.findAll())[1];
        BillItem secondBillItem = Helper.getFirstItem(secondBill.getItems());
        assertThat(secondBillItem.getItem(), is(billItem.getItem()));
        assertThat(secondBillItem.getQuantity(), is(2));
    }

}
