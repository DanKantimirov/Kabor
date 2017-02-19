package ru.kabor.demand.prediction.entity;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.EnumType;
import javax.persistence.Enumerated;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.Table;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import ru.kabor.demand.prediction.utils.FORECAST_METHOD;
import ru.kabor.demand.prediction.utils.SMOOTH_TYPE;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Table(name = "v_forecast_parameter")
public class ForecastParameter {
	
    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private int id;
    
    @ManyToOne(optional = false)
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
