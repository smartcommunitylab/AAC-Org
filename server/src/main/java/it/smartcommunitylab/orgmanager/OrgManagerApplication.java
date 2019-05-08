package it.smartcommunitylab.orgmanager;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.ComponentScan;

@SpringBootApplication
@ComponentScan(basePackages={"it.smartcommunitylab.orgmanager", "it.smartcommunitylab.nificonnector", "it.smartcommunitylab.apimconnector"})
public class OrgManagerApplication {

	public static void main(String[] args) {
		SpringApplication.run(OrgManagerApplication.class, args);
	}

}
