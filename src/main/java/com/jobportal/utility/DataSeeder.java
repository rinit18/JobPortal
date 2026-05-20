package com.jobportal.utility;

import java.time.LocalDateTime;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.stereotype.Component;

import com.jobportal.dto.JobStatus;
import com.jobportal.entity.Job;
import com.jobportal.entity.FAQ;
import com.jobportal.repository.JobRepository;
import com.jobportal.repository.FAQRepository;

@Component // Temporarily re-enabled to seed Mongo Atlas
public class DataSeeder implements ApplicationRunner {

    @Autowired
    private JobRepository jobRepository;

    @Autowired
    private FAQRepository faqRepository;

    @Override
    public void run(ApplicationArguments args) throws Exception {
        // Patch any existing jobs that have null jobStatus so they show up
        List<Job> allJobs = jobRepository.findAll();
        long nullStatusCount = allJobs.stream().filter(j -> j.getJobStatus() == null).count();
        if (nullStatusCount > 0) {
            allJobs.stream()
                .filter(j -> j.getJobStatus() == null)
                .forEach(j -> j.setJobStatus(JobStatus.ACTIVE));
            jobRepository.saveAll(allJobs);
            System.out.println("[DataSeeder] Patched " + nullStatusCount + " jobs with null status → ACTIVE.");
        }

        if (jobRepository.count() > 0) {
            System.out.println("[DataSeeder] Jobs already exist (" + jobRepository.count() + "), skipping seed.");
            return;
        }

        System.out.println("[DataSeeder] Seeding mock job data...");

        List<Job> jobs = List.of(
            job(1L, "Product Designer", "Meta",
                "Meta is seeking a Product Designer to join our team. You'll be working on designing user-centric interfaces for our blockchain wallet platform.",
                "Entry Level", "Full Time", "New York", 3200000L,
                List.of("Figma", "User Research", "Wireframing", "Prototyping")),
            job(2L, "Sr. UX Designer", "Netflix",
                "Netflix is looking for a Sr. UX Designer to enhance our user experience on streaming platforms.",
                "Expert", "Part Time", "San Francisco", 4000000L,
                List.of("UX Research", "Interaction Design", "Adobe XD", "Accessibility")),
            job(3L, "Product Designer", "Microsoft",
                "Join Microsoft as a Product Designer and contribute to our new Lightspeed studio. Create intuitive gaming experiences.",
                "Intermediate", "Full Time", "Remote", 3500000L,
                List.of("Figma", "Gaming UX", "Design Systems", "Collaboration")),
            job(4L, "Product Designer", "Adobe",
                "Adobe is seeking a part-time Product Designer to enhance user experience across our creative platforms.",
                "Expert", "Part Time", "Toronto", 3300000L,
                List.of("Adobe XD", "Sketch", "Design Thinking", "Prototyping")),
            job(5L, "Backend Developer", "Google",
                "Google is hiring a Backend Developer in Bangalore. Develop scalable backend systems that power our global services.",
                "Entry Level", "Full Time", "Bangalore", 3800000L,
                List.of("Java", "Spring Boot", "Cloud", "Distributed Systems")),
            job(6L, "SMM Manager", "Spotify",
                "Spotify is looking for an SMM Manager to lead social media marketing efforts and drive growth for our music platform.",
                "Intermediate", "Full Time", "Delhi", 3400000L,
                List.of("Social Media", "Content Strategy", "Analytics", "Campaign Management")),
            job(7L, "Frontend Developer", "Amazon",
                "Amazon is looking for a Frontend Developer to build and maintain customer-facing applications and create seamless web experiences.",
                "Intermediate", "Full Time", "Seattle", 3600000L,
                List.of("React", "TypeScript", "CSS", "Performance Optimization")),
            job(8L, "iOS Developer", "Apple",
                "Apple is seeking an iOS Developer to develop cutting-edge applications for iOS devices with high performance and exceptional UX.",
                "Expert", "Full Time", "Cupertino", 4200000L,
                List.of("Swift", "UIKit", "SwiftUI", "Xcode", "Core Data")),
            job(9L, "Data Scientist", "Tesla",
                "Tesla is looking for a Data Scientist to analyze large datasets and develop machine learning models for autonomous driving.",
                "Expert", "Full Time", "Austin", 5000000L,
                List.of("Python", "TensorFlow", "Machine Learning", "Data Visualization")),
            job(10L, "DevOps Engineer", "Airbnb",
                "Airbnb is hiring a DevOps Engineer to manage cloud infrastructure and CI/CD pipelines at scale.",
                "Intermediate", "Full Time", "Remote", 4500000L,
                List.of("Docker", "Kubernetes", "AWS", "Terraform", "CI/CD")),
            job(11L, "Full Stack Developer", "Flipkart",
                "Flipkart needs a Full Stack Developer to build scalable e-commerce features from end to end.",
                "Intermediate", "Full Time", "Bangalore", 2800000L,
                List.of("React", "Node.js", "MongoDB", "REST APIs")),
            job(12L, "Android Developer", "Samsung",
                "Samsung seeks an Android Developer to build and optimize high-performance Android applications.",
                "Intermediate", "Full Time", "Hyderabad", 3200000L,
                List.of("Kotlin", "Android SDK", "Jetpack Compose", "Firebase"))
        );

        jobRepository.saveAll(jobs);
        System.out.println("[DataSeeder] Seeded " + jobs.size() + " jobs successfully.");

        if (faqRepository.count() == 0) {
            List<FAQ> faqs = List.of(
                new FAQ(null, "How do I create a profile?", "You can create a profile by signing up as an Applicant and navigating to the Profile section from the top-right menu. We recommend using the 'Import from Resume' feature to automatically populate your details."),
                new FAQ(null, "How does the AI Match Score work?", "Our AI Match Score uses an advanced language model to compare your profile's skills, experience, and summary against the job description. It generates a percentage fit, along with identified strengths and gaps."),
                new FAQ(null, "Can employers see my saved jobs?", "No, your saved jobs are completely private. Employers can only see your profile when you explicitly apply for their job postings."),
                new FAQ(null, "What file formats are supported for resume upload?", "We currently support text-based PDF files. Scanned images or image-based PDFs cannot be parsed by our AI yet. Please ensure your PDF is selectable text with a maximum size of 20MB."),
                new FAQ(null, "How do I contact a recruiter?", "Once you apply for a job, you can use the built-in 'Messages' feature to communicate directly with the recruiter or employer who posted the job.")
            );
            faqRepository.saveAll(faqs);
            System.out.println("[DataSeeder] Seeded 5 FAQs successfully.");
        }
    }

    private Job job(Long id, String title, String company, String about,
                    String experience, String jobType, String location, Long salary,
                    List<String> skills) {
        Job j = new Job();
        j.setId(id);
        j.setJobTitle(title);
        j.setCompany(company);
        j.setAbout(about);
        j.setExperience(experience);
        j.setJobType(jobType);
        j.setLocation(location);
        j.setPackageOffered(salary);
        j.setPostTime(LocalDateTime.now().minusDays((long)(Math.random() * 20) + 1));
        j.setDescription("<p>" + about + "</p>");
        j.setSkillsRequired(skills);
        j.setJobStatus(JobStatus.ACTIVE);
        j.setPostedBy(1L);
        return j;
    }
}
