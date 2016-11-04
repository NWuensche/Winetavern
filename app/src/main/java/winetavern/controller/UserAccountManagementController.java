package winetavern.controller;

import winetavern.AccountCredentials;
import org.salespointframework.useraccount.UserAccount;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.RequestMapping;
import org.salespointframework.useraccount.UserAccountManager;
import org.springframework.web.bind.annotation.RequestMethod;


/**
 * Controller to store a new User in the DB.
 * @author michel
 */

@Controller
public class UserAccountManagementController {

    UserAccountManager userAccountManager;

    @Autowired
    public UserAccountManagementController(UserAccountManager userAccountManager) {
        this.userAccountManager = userAccountManager;
    }

    @RequestMapping(value="/admin/addNew", method=RequestMethod.POST)
    public String index(@ModelAttribute(value="accountcredentials") AccountCredentials registerCredentials) {
        UserAccount account = userAccountManager.create(registerCredentials.getUsername(), registerCredentials.getPassword());

        userAccountManager.save(account);

        return "redirect:/admin/users";
    }

    public UserAccountManager getUserAccountManager(){
        return userAccountManager;
    }

}