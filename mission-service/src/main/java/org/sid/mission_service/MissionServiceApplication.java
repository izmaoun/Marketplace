package org.sid.mission_service;

import org.sid.mission_service.entities.Mission;
import org.sid.mission_service.entities.MissionStatus;
import org.sid.mission_service.entities.WorkMode;
import org.sid.mission_service.repositories.MissionRepository;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;

import java.math.BigDecimal;
import java.util.Arrays;
import java.util.List;

@SpringBootApplication
public class MissionServiceApplication {

	public static void main(String[] args) {
		SpringApplication.run(MissionServiceApplication.class, args);
	}

	@Bean
	CommandLineRunner initData(MissionRepository missionRepository) {
		return args -> {
			// Vérifier si la base est vide avant d'insérer
			if (missionRepository.count() == 0) {

				// Mission 1 : Développeur Java (Remote)
				Mission mission1 = Mission.builder()
						.companyId(1L)
						.title("Développeur Java Backend")
						.description("Recherche développeur Java expérimenté pour développer des microservices avec Spring Boot")
						.requiredSkills(Arrays.asList("Java", "Spring Boot", "Microservices", "JPA", "Maven"))
						.durationDays(30)
						.budget(new BigDecimal(4500))
						.workMode(WorkMode.REMOTE)
						.status(MissionStatus.PUBLIEE)
						.build();

				// Mission 2 : Frontend React (Présentiel)
				Mission mission2 = Mission.builder()
						.companyId(1L)
						.title("Développeur Frontend React")
						.description("Création d'une interface utilisateur moderne avec React et TypeScript")
						.requiredSkills(Arrays.asList("React", "TypeScript", "HTML5/CSS3", "Redux", "Tailwind"))
						.durationDays(25)
						.budget(new BigDecimal(3800))
						.workMode(WorkMode.PRESENTIEL)
						.status(MissionStatus.PUBLIEE)
						.build();

				// Mission 3 : DevOps (Hybride)
				Mission mission3 = Mission.builder()
						.companyId(2L)
						.title("Ingénieur DevOps")
						.description("Mise en place des pipelines CI/CD et infrastructure cloud")
						.requiredSkills(Arrays.asList("Docker", "Kubernetes", "Jenkins", "AWS", "Terraform"))
						.durationDays(45)
						.budget(new BigDecimal(6200))
						.workMode(WorkMode.HYBRIDE)
						.status(MissionStatus.PUBLIEE)
						.build();

				// Mission 4 : Data Scientist (Remote)
				Mission mission4 = Mission.builder()
						.companyId(2L)
						.title("Data Scientist")
						.description("Analyse de données et création de modèles prédictifs")
						.requiredSkills(Arrays.asList("Python", "Pandas", "Scikit-learn", "SQL", "TensorFlow"))
						.durationDays(40)
						.budget(new BigDecimal(5500))
						.workMode(WorkMode.REMOTE)
						.status(MissionStatus.PUBLIEE)
						.build();

				// Mission 5 : Mobile Flutter (En cours)
				Mission mission5 = Mission.builder()
						.companyId(3L)
						.title("Développeur Mobile Flutter")
						.description("Développement d'une application mobile cross-platform")
						.requiredSkills(Arrays.asList("Flutter", "Dart", "Firebase", "REST API", "Git"))
						.durationDays(35)
						.budget(new BigDecimal(5000))
						.workMode(WorkMode.HYBRIDE)
						.status(MissionStatus.EN_COURS)
						.build();

				// Mission 6 : Admin Sys (Brouillon)
				Mission mission6 = Mission.builder()
						.companyId(3L)
						.title("Administrateur Systèmes Linux")
						.description("Gestion et maintenance des serveurs Linux")
						.requiredSkills(Arrays.asList("Linux", "Bash", "NGINX", "MySQL", "Monitoring"))
						.durationDays(20)
						.budget(new BigDecimal(3500))
						.workMode(WorkMode.PRESENTIEL)
						.status(MissionStatus.BROUILLON)
						.build();

				// Mission 7 : Sécurité (Publiée)
				Mission mission7 = Mission.builder()
						.companyId(1L)
						.title("Expert en Cybersécurité")
						.description("Audit de sécurité et mise en place des bonnes pratiques")
						.requiredSkills(Arrays.asList("Cybersécurité", "Pentest", "ISO 27001", "Firewall", "Cryptographie"))
						.durationDays(50)
						.budget(new BigDecimal(7500))
						.workMode(WorkMode.REMOTE)
						.status(MissionStatus.PUBLIEE)
						.build();

				// Mission 8 : QA Testeur (Clôturée)
				Mission mission8 = Mission.builder()
						.companyId(2L)
						.title("Testeur QA Automatisation")
						.description("Mise en place de tests automatisés E2E et d'intégration")
						.requiredSkills(Arrays.asList("Selenium", "JUnit", "Cypress", "Jenkins", "Postman"))
						.durationDays(28)
						.budget(new BigDecimal(4200))
						.workMode(WorkMode.HYBRIDE)
						.status(MissionStatus.CLOTUREE)
						.build();

				// Sauvegarde de toutes les missions
				List<Mission> missions = Arrays.asList(
						mission1, mission2, mission3, mission4,
						mission5, mission6, mission7, mission8
				);

				missionRepository.saveAll(missions);

				System.out.println("=== 8 missions de démonstration insérées avec succès ===");
				System.out.println("Statistiques des missions :");
				System.out.println("- Publiées: " + missions.stream().filter(m -> m.getStatus() == MissionStatus.PUBLIEE).count());
				System.out.println("- En cours: " + missions.stream().filter(m -> m.getStatus() == MissionStatus.EN_COURS).count());
				System.out.println("- Brouillon: " + missions.stream().filter(m -> m.getStatus() == MissionStatus.BROUILLON).count());
				System.out.println("- Clôturées: " + missions.stream().filter(m -> m.getStatus() == MissionStatus.CLOTUREE).count());

			} else {
				System.out.println("Base de données déjà initialisée - " + missionRepository.count() + " missions existantes");
			}
		};
	}
}