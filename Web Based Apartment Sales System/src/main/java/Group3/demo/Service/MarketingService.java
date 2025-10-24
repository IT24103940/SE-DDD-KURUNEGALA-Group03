package Group3.demo.Service;

import Group3.demo.Entity.Promotion;

import java.util.List;

public interface MarketingService {
    Promotion create(Promotion p);
    List<Promotion> list();
    void delete(Integer id);
}
