package com.fitpulse.service.impl;

import com.fitpulse.model.dto.MembershipRequest;
import com.fitpulse.model.dto.MembershipView;
import com.fitpulse.model.entity.Membership;
import com.fitpulse.model.enums.MembershipType;
import com.fitpulse.repository.MembershipRepository;
import com.fitpulse.repository.UserRepository;
import com.fitpulse.service.MembershipService;
import com.fitpulse.service.UserService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cache.CacheManager;
import org.springframework.cache.annotation.EnableCaching;
import org.springframework.cache.concurrent.ConcurrentMapCacheManager;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import org.springframework.test.util.ReflectionTestUtils;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.clearInvocations;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(SpringExtension.class)
@ContextConfiguration(classes = MembershipServiceCachingTest.TestConfig.class)
class MembershipServiceCachingTest {

    @Autowired
    private MembershipService membershipService;

    @Autowired
    private MembershipRepository membershipRepository;

    @Autowired
    private CacheManager cacheManager;

    @BeforeEach
    void setUp() {
        cacheManager.getCache("memberships").clear();
        org.mockito.Mockito.reset(membershipRepository);
    }

    @Test
    void getAllCachesImmutableMembershipViews() {
        Membership membership = membership(UUID.randomUUID(), "Basic");
        when(membershipRepository.findAllByOrderByPriceAsc())
                .thenReturn(List.of(membership));

        List<MembershipView> firstResult = membershipService.getAll();
        List<MembershipView> secondResult = membershipService.getAll();

        assertSame(firstResult, secondResult);
        assertEquals("Basic", secondResult.getFirst().title());
        assertThrows(UnsupportedOperationException.class,
                () -> secondResult.add(secondResult.getFirst()));
        verify(membershipRepository).findAllByOrderByPriceAsc();
    }

    @Test
    void createEvictsCachedMembershipList() {
        assertCacheIsReloadedAfter(() ->
                membershipService.create(request(MembershipType.PREMIUM, "Premium")));
    }

    @Test
    void updateEvictsCachedMembershipList() {
        UUID id = UUID.randomUUID();
        Membership membership = membership(id, "Basic");
        when(membershipRepository.findById(id)).thenReturn(Optional.of(membership));
        when(membershipRepository.findByType(MembershipType.PREMIUM))
                .thenReturn(Optional.empty());

        assertCacheIsReloadedAfter(() -> membershipService.update(
                id, request(MembershipType.PREMIUM, "Updated")));
    }

    @Test
    void deleteEvictsCachedMembershipList() {
        UUID id = UUID.randomUUID();
        Membership membership = membership(id, "Basic");
        when(membershipRepository.findById(id)).thenReturn(Optional.of(membership));

        assertCacheIsReloadedAfter(() -> membershipService.delete(id));
    }

    private void assertCacheIsReloadedAfter(Runnable stateChange) {
        Membership initialMembership = membership(UUID.randomUUID(), "Initial");
        Membership refreshedMembership = membership(UUID.randomUUID(), "Refreshed");
        when(membershipRepository.findAllByOrderByPriceAsc())
                .thenReturn(List.of(initialMembership), List.of(refreshedMembership));

        membershipService.getAll();
        clearInvocations(membershipRepository);

        stateChange.run();
        List<MembershipView> refreshedResult = membershipService.getAll();

        assertEquals("Refreshed", refreshedResult.getFirst().title());
        verify(membershipRepository).findAllByOrderByPriceAsc();
    }

    private Membership membership(UUID id, String title) {
        Membership membership = new Membership();
        ReflectionTestUtils.setField(membership, "id", id);
        membership.setType(MembershipType.BASIC);
        membership.setTitle(title);
        membership.setPrice(new BigDecimal("20.00"));
        membership.setDurationDays(30);
        membership.setDescription(title + " membership");
        return membership;
    }

    private MembershipRequest request(MembershipType type, String title) {
        MembershipRequest request = new MembershipRequest();
        request.setType(type);
        request.setTitle(title);
        request.setPrice(new BigDecimal("30.00"));
        request.setDurationDays(30);
        request.setDescription(title + " membership");
        return request;
    }

    @Configuration
    @EnableCaching
    static class TestConfig {

        @Bean
        MembershipRepository membershipRepository() {
            return mock(MembershipRepository.class);
        }

        @Bean
        UserRepository userRepository() {
            return mock(UserRepository.class);
        }

        @Bean
        UserService userService() {
            return mock(UserService.class);
        }

        @Bean
        MembershipService membershipService(
                MembershipRepository membershipRepository,
                UserRepository userRepository,
                UserService userService) {

            return new MembershipServiceImpl(
                    membershipRepository, userRepository, userService);
        }

        @Bean
        CacheManager cacheManager() {
            return new ConcurrentMapCacheManager("memberships");
        }
    }
}
