package org.example.eventplatform.catalog.config;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.example.eventplatform.catalog.entity.ServiceCategory;
import org.example.eventplatform.catalog.repository.ServiceCategoryRepository;
import org.springframework.boot.ApplicationRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.List;

/**
 * Danh mục dịch vụ là trục phân loại của sàn khách hàng, nên phải có sẵn từ lần
 * chạy đầu. {@code code} khớp với {@code Tenant.category} để nhóm được các đơn vị
 * hiện có mà không phải backfill dữ liệu.
 */
@Configuration
@RequiredArgsConstructor
@Slf4j
public class DataSeeder {

    private record Seed(String code, String name, String description) {
    }

    private static final List<Seed> BASELINE_CATEGORIES = List.of(
            new Seed("LION_DANCE", "Lân Sư Rồng", "Múa lân, múa rồng, trống hội cho khai trương và lễ hội"),
            new Seed("MUSIC", "Ca nhạc", "Ban nhạc, ca sĩ, nhóm nhảy biểu diễn sự kiện"),
            new Seed("MC", "Dẫn chương trình", "MC cho tiệc cưới, sự kiện doanh nghiệp"),
            new Seed("SOUND_LIGHT", "Âm thanh ánh sáng", "Thiết bị âm thanh, ánh sáng, sân khấu"),
            new Seed("WEDDING_PLANNER", "Trang trí tiệc cưới", "Trang trí, cổng hoa, backdrop tiệc cưới")
    );

    @Bean
    public ApplicationRunner seedServiceCategories(ServiceCategoryRepository repository) {
        return args -> BASELINE_CATEGORIES.forEach(seed -> {
            if (repository.findByCode(seed.code()).isEmpty()) {
                repository.save(ServiceCategory.builder()
                        .code(seed.code())
                        .name(seed.name())
                        .description(seed.description())
                        .active(true)
                        .build());
                log.info("Seeded service category {}", seed.code());
            }
        });
    }
}
