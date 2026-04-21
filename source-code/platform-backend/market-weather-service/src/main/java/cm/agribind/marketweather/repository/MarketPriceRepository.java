package cm.agribind.marketweather.repository;

import cm.agribind.marketweather.model.MarketPriceEntity;
import cm.agribind.marketweather.model.PriceSource;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface MarketPriceRepository extends JpaRepository<MarketPriceEntity, Long> {

    Optional<MarketPriceEntity> findByCommodityCodeAndMarket(String commodityCode, String market);

    List<MarketPriceEntity> findByPriceSource(PriceSource priceSource);

    List<MarketPriceEntity> findAllByOrderByUpdatedAtDesc();
}
