package com.fitpulse.config;

import com.fitpulse.model.entity.GymClass;
import com.fitpulse.model.entity.Membership;
import com.fitpulse.model.entity.User;
import com.fitpulse.model.enums.ClassIntensity;
import com.fitpulse.model.enums.MembershipType;
import com.fitpulse.model.enums.UserRole;
import com.fitpulse.repository.GymClassRepository;
import com.fitpulse.repository.MembershipRepository;
import com.fitpulse.repository.UserRepository;
import org.springframework.boot.CommandLineRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;
import java.math.BigDecimal;
import java.time.LocalDateTime;

@Component
public class DataSeeder implements CommandLineRunner {
    private final UserRepository userRepository; private final MembershipRepository membershipRepository; private final GymClassRepository gymClassRepository; private final PasswordEncoder passwordEncoder;
    public DataSeeder(UserRepository userRepository, MembershipRepository membershipRepository, GymClassRepository gymClassRepository, PasswordEncoder passwordEncoder) { this.userRepository = userRepository; this.membershipRepository = membershipRepository; this.gymClassRepository = gymClassRepository; this.passwordEncoder = passwordEncoder; }
    @Override
    public void run(String... args) {
        if (membershipRepository.count() == 0) {
            membershipRepository.save(make(MembershipType.BASIC, "Basic Pulse", "Access to standard group classes and basic facilities.", "39.00", 30));
            membershipRepository.save(make(MembershipType.PREMIUM, "Premium Pulse", "More classes, priority booking and extended gym access.", "69.00", 30));
            membershipRepository.save(make(MembershipType.VIP, "VIP Pulse", "All classes, personal support and premium benefits.", "99.00", 30));
        }
        if (userRepository.count() == 0) {
            User admin = new User(); admin.setUsername("admin"); admin.setEmail("admin@fitpulse.com"); admin.setPassword(passwordEncoder.encode("admin123")); admin.setRole(UserRole.ADMIN); userRepository.save(admin);
            User member = new User(); member.setUsername("member"); member.setEmail("member@fitpulse.com"); member.setPassword(passwordEncoder.encode("member123")); member.setRole(UserRole.MEMBER); member.setMembership(membershipRepository.findByType(MembershipType.PREMIUM).orElse(null)); userRepository.save(member);
        }
        if (gymClassRepository.count() == 0) {
            Membership basic = membershipRepository.findByType(MembershipType.BASIC).orElseThrow();
            GymClass yoga = new GymClass(); yoga.setTitle("Morning Yoga Flow"); yoga.setTrainerName("Maria Petrova"); yoga.setStartsAt(LocalDateTime.now().plusDays(1).withHour(8).withMinute(0)); yoga.setCapacity(20); yoga.setIntensity(ClassIntensity.LOW); yoga.setDescription("A calm morning class focused on mobility, breathing and posture."); yoga.setRequiredMembership(basic); gymClassRepository.save(yoga);
            GymClass hiit = new GymClass(); hiit.setTitle("HIIT Power Session"); hiit.setTrainerName("Ivan Georgiev"); hiit.setStartsAt(LocalDateTime.now().plusDays(2).withHour(18).withMinute(30)); hiit.setCapacity(15); hiit.setIntensity(ClassIntensity.HIGH); hiit.setDescription("A fast-paced workout for strength, conditioning and fat burning."); hiit.setRequiredMembership(basic); gymClassRepository.save(hiit);
        }
    }
    private Membership make(MembershipType type, String title, String description, String price, int days) { Membership m = new Membership(); m.setType(type); m.setTitle(title); m.setDescription(description); m.setPrice(new BigDecimal(price)); m.setDurationDays(days); return m; }
}
