package com.helpdesk.controller;

import com.helpdesk.dto.ApiResponse;
import com.helpdesk.entity.*;
import com.helpdesk.repository.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;

@RestController
@RequestMapping("/api/admin")
public class DataGenerationController {

    @Autowired
    private CompanyRepository companyRepository;

    @Autowired
    private DepartmentRepository departmentRepository;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private TicketRepository ticketRepository;

    @Autowired
    private TicketCommentRepository ticketCommentRepository;

    @Autowired
    private AttachmentRepository attachmentRepository;

    @Autowired
    private RoleRepository roleRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @Autowired
    private JdbcTemplate jdbcTemplate;

    private final Random random = new Random();

    @PostMapping("/generate-data")
    @Transactional
    public ResponseEntity<ApiResponse<String>> generateDemoData() {
        // 1. Reset Database Cleanly
        jdbcTemplate.execute("SET FOREIGN_KEY_CHECKS = 0");
        jdbcTemplate.execute("TRUNCATE TABLE attachments");
        jdbcTemplate.execute("TRUNCATE TABLE ticket_comments");
        jdbcTemplate.execute("TRUNCATE TABLE tickets");
        jdbcTemplate.execute("TRUNCATE TABLE notifications");
        jdbcTemplate.execute("TRUNCATE TABLE refresh_tokens");
        jdbcTemplate.execute("TRUNCATE TABLE password_reset_tokens");
        jdbcTemplate.execute("TRUNCATE TABLE activity_logs");
        jdbcTemplate.execute("DELETE FROM users WHERE role_id != 1"); // Keep Super Admin (role_id = 1)
        jdbcTemplate.execute("DELETE FROM departments");
        jdbcTemplate.execute("DELETE FROM companies");
        jdbcTemplate.execute("SET FOREIGN_KEY_CHECKS = 1");

        // Fetch standard roles
        Role superAdminRole = roleRepository.findByName(RoleName.ROLE_SUPER_ADMIN)
                .orElseThrow(() -> new RuntimeException("Super admin role not initialized"));
        Role companyAdminRole = roleRepository.findByName(RoleName.ROLE_COMPANY_ADMIN)
                .orElseThrow(() -> new RuntimeException("Company admin role not initialized"));
        Role agentRole = roleRepository.findByName(RoleName.ROLE_SUPPORT_AGENT)
                .orElseThrow(() -> new RuntimeException("Agent role not initialized"));
        Role customerRole = roleRepository.findByName(RoleName.ROLE_CUSTOMER)
                .orElseThrow(() -> new RuntimeException("Customer role not initialized"));

        // Re-generate default TechNova Solutions Company & default seed users
        Company techNova = Company.builder()
                .name("TechNova Solutions")
                .email("info@technova.com")
                .phone("123-456-7890")
                .address("100 Innovation Way, Tech City")
                .active(true)
                .build();
        techNova = companyRepository.save(techNova);

        User techNovaAdmin = User.builder()
                .fullName("TechNova Administrator")
                .email("companyadmin@technova.com")
                .password(passwordEncoder.encode("Admin@123"))
                .phone("888-888-8888")
                .role(companyAdminRole)
                .company(techNova)
                .active(true)
                .build();
        userRepository.save(techNovaAdmin);

        User techNovaAgent = User.builder()
                .fullName("Sarah Connor")
                .email("agent1@technova.com")
                .password(passwordEncoder.encode("Agent@123"))
                .phone("777-777-7777")
                .role(agentRole)
                .company(techNova)
                .active(true)
                .build();
        userRepository.save(techNovaAgent);

        User techNovaCustomer = User.builder()
                .fullName("John Doe")
                .email("customer1@gmail.com")
                .password(passwordEncoder.encode("Customer@123"))
                .phone("666-666-6666")
                .role(customerRole)
                .company(techNova)
                .active(true)
                .build();
        userRepository.save(techNovaCustomer);

        // Generate the 3 Demo SaaS Companies
        String[] companyNames = {"Acme Corporation", "Globex Systems", "Initech Soft"};
        String[] companyEmails = {"contact@acme.com", "info@globex.com", "support@initech.com"};
        String[] companyPhones = {"555-0199", "555-0211", "555-0322"};
        String[] companyAddresses = {"123 Industrial Way, Metropolis", "456 Cyber Ridge, Neo-Tokyo", "4120 Freemont Blvd, Silicon Valley"};

        String[][] departmentNames = {
            {"IT Technical Support", "Billing & Licences", "Infrastructure & Hardware"},
            {"Customer Care", "Accounts & Finance", "Engineering Operations"},
            {"Helpdesk Support", "Premium Enterprise SLA", "Network Operations Centre"}
        };

        String[] customerNames = {
            "Alice Jenkins", "Bob Miller", "Charlie Smith", "Diana Prince", "Ethan Hunt", 
            "Fiona Gallagher", "George Costanza", "Hannah Abbott", "Ian Malcolm", "Julia Roberts",
            "Kevin Bacon", "Laura Croft", "Michael Scott", "Nancy Wheeler", "Oscar Martinez",
            "Pam Beesly", "Quentin Tarantino", "Rachel Green", "Steve Rogers", "Tony Stark"
        };

        String[] agentNames = {
            "Sarah Connor", "John Connor", "Ellen Ripley", "Peter Parker", "Bruce Wayne",
            "Clark Kent", "Barry Allen", "Diana Prince", "Hal Jordan", "Arthur Curry"
        };

        String[] ticketTitles = {
            "VPN connectivity dropping frequently",
            "Need assistance setting up new corporate laptop",
            "Error code 500 when accessing payroll portal",
            "Office 365 license renewal failure",
            "Request for guest Wi-Fi access credentials",
            "Slow performance on remote desktop connection",
            "Printer in block B paper jam issue",
            "Unable to send outgoing emails to external domains",
            "Monitors flickers when connected to dock",
            "Missing access permissions to shared project folder",
            "Requesting replacement battery for notebook",
            "SaaS billing discrepancy on latest invoice",
            "Symmetric key decryption error in app log",
            "Network switch port activation request",
            "Corporate mobile device enrollment failing"
        };

        String[] ticketDescriptions = {
            "Every time I connect to the corporate VPN, it disconnects after approximately 10 minutes. I need this to work for my remote sessions.",
            "I received my new laptop today. I need support to walk me through the initial setup, software installs, and active directory login.",
            "Getting a generic HTTP 500 Internal Server Error when trying to download my monthly payslips from the portal.",
            "My Office 365 applications say my enterprise license is expired. Please help resolve this so I can access Word and Excel.",
            "We have visiting clients today who require high-speed internet access. Please provide the SSID and passphrase for guests.",
            "The remote desktop session has severe latency, making typing and clicking almost unusable. Speedtest shows my local line is fast.",
            "The large laser printer in Block B hallway is showing a paper jam alert, but we cannot locate any stuck paper in the drawers.",
            "Any email sent to Gmail or Yahoo addresses is bouncing back with SPF/DKIM verification failure. Internal emails work fine.",
            "The second display connected via the USB-C dock occasionally goes black for 2 seconds then comes back. Driver update tried.",
            "I need write permissions to the marketing archive folder under the Shared drive. My department code has changed.",
            "My laptop battery is swelling up and drains in 15 minutes. It represents a potential hazard and needs immediate swap.",
            "The recent license invoice lists 55 seats, but we only have 48 active accounts registered. Please adjust and credit.",
            "Getting repeated security alerts about decryption failure on node cluster. Trace log shows missing encryption key ref.",
            "I am in meeting room 3B. The Ethernet port on the wall is not providing an IP address. Need it enabled for video feed.",
            "The endpoint manager app returns profile validation error during registration. I followed the enrollment instruction doc."
        };

        String[] ticketCategories = {"Software", "Hardware", "Network", "Billing", "General"};

        String[] publicComments = {
            "I have updated your ticket status. Could you please check if the issue persists now?",
            "Thanks for reporting. Our engineering team is currently investigating this incident.",
            "Could you please share a screenshot of the error code or logs?",
            "I tried restarting the adapter and it worked for a few minutes but failed again.",
            "This has been resolved. The licensing pool has been replenished. Please verify."
        };

        String[] internalNotes = {
            "Escalating this to tier-2 network engineers for log verification.",
            "User is a premium tenant customer. Please expedite.",
            "Checked local logs. Appears to be an AD replication sync mismatch.",
            "Hardware is out of warranty. Suggesting replacement unit approval."
        };

        int companyCount = 0;
        int departmentCount = 0;
        int userCount = 0;
        int ticketCount = 0;

        for (int i = 0; i < companyNames.length; i++) {
            // Create Company
            Company company = Company.builder()
                    .name(companyNames[i])
                    .email(companyEmails[i])
                    .phone(companyPhones[i])
                    .address(companyAddresses[i])
                    .active(true)
                    .build();
            company = companyRepository.save(company);
            companyCount++;

            // Create Company Admin
            String adminEmail = "admin@" + companyNames[i].toLowerCase().replace(" ", "") + ".com";
            User companyAdmin = User.builder()
                    .fullName(companyNames[i] + " Administrator")
                    .email(adminEmail)
                    .password(passwordEncoder.encode("Admin@123"))
                    .phone("555-081" + i)
                    .role(companyAdminRole)
                    .company(company)
                    .active(true)
                    .build();
            userRepository.save(companyAdmin);
            userCount++;

            // Create Departments for this Company
            List<Department> depts = new ArrayList<>();
            for (String deptName : departmentNames[i]) {
                Department department = Department.builder()
                        .name(deptName)
                        .description("Support and operations for " + deptName)
                        .company(company)
                        .build();
                department = departmentRepository.save(department);
                depts.add(department);
                departmentCount++;
            }

            // Create Support Agents
            List<User> agents = new ArrayList<>();
            for (int j = 0; j < 3; j++) {
                String agentName = agentNames[(i * 3 + j) % agentNames.length];
                String agentEmail = agentName.toLowerCase().replace(" ", "") + "@" + companyNames[i].toLowerCase().replace(" ", "") + ".com";
                Department assignedDept = depts.get(j % depts.size());
                
                User agent = User.builder()
                        .fullName(agentName)
                        .email(agentEmail)
                        .password(passwordEncoder.encode("Agent@123"))
                        .phone("555-072" + (i * 3 + j))
                        .role(agentRole)
                        .company(company)
                        .department(assignedDept)
                        .active(true)
                        .build();
                agent = userRepository.save(agent);
                agents.add(agent);
                userCount++;
            }

            // Create Customers
            List<User> customers = new ArrayList<>();
            for (int j = 0; j < 6; j++) {
                String custName = customerNames[(i * 6 + j) % customerNames.length];
                String custEmail = custName.toLowerCase().replace(" ", "") + "@gmail.com";
                
                User customer = User.builder()
                        .fullName(custName)
                        .email(custEmail)
                        .password(passwordEncoder.encode("Customer@123"))
                        .phone("555-063" + (i * 6 + j))
                        .role(customerRole)
                        .company(company)
                        .active(true)
                        .build();
                customer = userRepository.save(customer);
                customers.add(customer);
                userCount++;
            }

            // Create Tickets
            for (int k = 0; k < 12; k++) {
                int ticketSeed = random.nextInt(ticketTitles.length);
                String title = ticketTitles[ticketSeed];
                String desc = ticketDescriptions[ticketSeed];
                String category = ticketCategories[random.nextInt(ticketCategories.length)];
                
                TicketStatus[] statuses = TicketStatus.values();
                TicketStatus status = statuses[random.nextInt(statuses.length)];
                
                TicketPriority[] priorities = TicketPriority.values();
                TicketPriority priority = priorities[random.nextInt(priorities.length)];

                User customer = customers.get(random.nextInt(customers.size()));
                User agent = null;
                Department department = depts.get(random.nextInt(depts.size()));

                // Assign agents based on status
                if (status != TicketStatus.OPEN) {
                    agent = agents.get(random.nextInt(agents.size()));
                    if (agent.getDepartment() != null) {
                        department = agent.getDepartment();
                    }
                }

                // Random dates over last 30 days
                LocalDateTime createdAt = LocalDateTime.now().minusDays(random.nextInt(28)).minusHours(random.nextInt(23));
                LocalDateTime resolvedAt = null;
                LocalDateTime closedAt = null;

                if (status == TicketStatus.RESOLVED) {
                    resolvedAt = createdAt.plusDays(random.nextInt(2) + 1).plusHours(random.nextInt(12));
                } else if (status == TicketStatus.CLOSED) {
                    resolvedAt = createdAt.plusDays(random.nextInt(2) + 1).plusHours(random.nextInt(12));
                    closedAt = resolvedAt.plusDays(random.nextInt(2));
                }

                Ticket ticket = Ticket.builder()
                        .title(title)
                        .description(desc)
                        .status(status)
                        .priority(priority)
                        .category(category)
                        .customer(customer)
                        .agent(agent)
                        .department(department)
                        .company(company)
                        .createdAt(createdAt)
                        .resolvedAt(resolvedAt)
                        .closedAt(closedAt)
                        .build();

                ticket = ticketRepository.save(ticket);
                ticketCount++;

                // Add comments
                int commentsCount = random.nextInt(4); // 0 to 3 comments
                for (int c = 0; c < commentsCount; c++) {
                    boolean isInternal = random.nextBoolean();
                    String commentText = isInternal 
                            ? internalNotes[random.nextInt(internalNotes.length)] 
                            : publicComments[random.nextInt(publicComments.length)];
                    
                    User commentUser = random.nextBoolean() ? customer : agents.get(random.nextInt(agents.size()));

                    TicketComment comment = TicketComment.builder()
                            .ticket(ticket)
                            .user(commentUser)
                            .commentText(commentText)
                            .internalNote(isInternal)
                            .createdAt(createdAt.plusHours(random.nextInt(24) + 1))
                            .build();

                    ticketCommentRepository.save(comment);
                }

                // Add attachment placeholder
                if (random.nextBoolean()) {
                    Attachment attachment = Attachment.builder()
                            .ticket(ticket)
                            .fileName("diagnostic_screenshot_" + ticket.getId() + ".png")
                            .filePath("demo_placeholder.png")
                            .fileType("image/png")
                            .fileSize(1024L * (random.nextInt(500) + 50))
                            .uploadedAt(createdAt)
                            .build();
                    attachmentRepository.save(attachment);
                }
            }
        }

        String resultMsg = String.format("Demo environment initialized successfully. Reset database and generated %d companies, %d departments, %d users, and %d tickets.",
                companyCount, departmentCount, userCount, ticketCount);
        return ResponseEntity.ok(new ApiResponse<>(true, resultMsg, resultMsg));
    }
}
