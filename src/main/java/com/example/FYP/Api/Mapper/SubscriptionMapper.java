package com.example.FYP.Api.Mapper;

import com.example.FYP.Api.Entity.Subscription;
import com.example.FYP.Api.Model.View.SubscriptionViewDTO;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
@Mapper(componentModel = "spring", uses = {BigDecimalMapper.class})
public abstract class SubscriptionMapper {

    @Mapping(source = "plan.subscriptionPlan", target = "plan")
    public abstract SubscriptionViewDTO toViewDTO(Subscription subscription);


}