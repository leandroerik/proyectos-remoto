package ar.com.hipotecario.canal.officebanking.jpa;

import java.util.HashMap;
import java.util.Map;

import com.zaxxer.hikari.HikariConfig;

import jakarta.persistence.EntityManagerFactory;
import jakarta.persistence.Persistence;

public class JPA {

	private static JPA manager = null;

	private EntityManagerFactory entityManagerFactory = null;

	private JPA(String unitName, HikariConfig config) {
		if (manager == null) {Map<String, String> props = new HashMap<String, String>();
			props.put("javax.persistence.jdbc.driver", config.getDriverClassName());
			props.put("javax.persistence.jdbc.url", config.getJdbcUrl());
			props.put("javax.persistence.jdbc.user", config.getUsername());
			props.put("javax.persistence.jdbc.password", config.getPassword());
			props.put("hibernate.hbm2ddl.auto", "validate");
			EntityManagerFactory entityManagerFactory = Persistence.createEntityManagerFactory(unitName, props);
			this.entityManagerFactory = entityManagerFactory;
		}
	}

	public static JPA getInstance(String unitName, HikariConfig config) {
		if (manager == null) {
			manager = new JPA(unitName, config);
		}
		return manager;
	}

	public EntityManagerFactory getEntityManager() {
		return this.entityManagerFactory;
	}
}
