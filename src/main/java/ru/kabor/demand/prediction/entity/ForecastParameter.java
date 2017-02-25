package ru.kabor.demand.prediction.entity;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import ru.kabor.demand.prediction.utils.FORECAST_METHOD;
import ru.kabor.demand.prediction.utils.SMOOTH_TYPE;

import javax.persistence.*;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Table(name = "v_forecast_parameter")
public class ForecastParameter {
	
    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private int id;
    
    @OneToOne(optional = false)
    @JoinColumn(name = "request_id", nullable = false)
    private Request request;
    
    @Column(name = "duration", nullable = false)
    private int duration;
    
    @Column(name = "forecast_method", length = 150, nullable = false)
    @Enumerated(EnumType.STRING)
    private FORECAST_METHOD forecast_method;
    
    @Column(name = "smoothing_method", length = 150, nullable = false)
    @Enumerated(EnumType.STRING)
    private SMOOTH_TYPE smoothing_method;
}
