package Group3.demo.Service.Impl;

import Group3.demo.Entity.Promotion;
import Group3.demo.Repository.PromotionRepository;
import Group3.demo.Service.MarketingService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

@Service @RequiredArgsConstructor
public class MarketingServiceImpl implements MarketingService {
    private final PromotionRepository repo;

    @Override public Promotion create(Promotion p) { return repo.save(p); }
    @Override public List<Promotion> list() { return repo.findAll(); }
    @Override public void delete(Integer id) { repo.deleteById(id); }
}
