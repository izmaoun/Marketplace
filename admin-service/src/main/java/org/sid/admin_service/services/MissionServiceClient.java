package org.sid.admin_service.services;


import org.springframework.cloud.openfeign.FeignClient;


@FeignClient(name = "mission-service", url = "http://mission-service:8084")
public interface MissionServiceClient {



}
