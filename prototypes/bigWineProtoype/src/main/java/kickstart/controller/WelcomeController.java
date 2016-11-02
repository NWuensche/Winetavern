/*
 * Copyright 2014-2015 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package kickstart.controller;

import org.salespointframework.useraccount.Role;
import org.salespointframework.useraccount.UserAccount;
import org.salespointframework.useraccount.UserAccountManager;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.RequestMapping;
import kickstart.RegisterCredentials;


@Controller
public class WelcomeController {

	@Autowired UserAccountManager manager;

	@RequestMapping("/")
	public String index(Model model) {
		RegisterCredentials registerCredentials = new RegisterCredentials();
		model.addAttribute("registercredentials", registerCredentials);

		addAdminToDBIfNotThereYet();

		return "login";
	}

	private void addAdminToDBIfNotThereYet() {
		String adminName = "admin";

		if(!adminInDB(adminName)) {
			UserAccount admin = manager.create(adminName, "asdf", Role.of("ADMIN"));
			manager.save(admin);
		}
	}

	private boolean adminInDB(String adminName) {
		return manager.findByUsername(adminName).isPresent();
	}

}
