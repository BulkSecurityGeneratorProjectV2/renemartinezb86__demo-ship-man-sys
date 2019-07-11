package com.amazon.salaunch.demo.web.rest;

import com.amazon.salaunch.demo.ShipManagementSystemApp;
import com.amazon.salaunch.demo.domain.Location;
import com.amazon.salaunch.demo.repository.LocationRepository;
import com.amazon.salaunch.demo.service.LocationService;
import com.amazon.salaunch.demo.web.rest.errors.ExceptionTranslator;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.MockitoAnnotations;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.data.web.PageableHandlerMethodArgumentResolver;
import org.springframework.http.MediaType;
import org.springframework.http.converter.json.MappingJackson2HttpMessageConverter;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.validation.Validator;


import java.util.List;

import static com.amazon.salaunch.demo.web.rest.TestUtil.createFormattingConversionService;
import static org.assertj.core.api.Assertions.assertThat;
import static org.hamcrest.Matchers.hasItem;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

/**
 * Integration tests for the {@Link LocationResource} REST controller.
 */
@SpringBootTest(classes = ShipManagementSystemApp.class)
public class LocationResourceIT {

    private static final String DEFAULT_SEA_QUADRANT = "AAAAAAAAAA";
    private static final String UPDATED_SEA_QUADRANT = "BBBBBBBBBB";

    private static final String DEFAULT_FRIENDLYS = "AAAAAAAAAA";
    private static final String UPDATED_FRIENDLYS = "BBBBBBBBBB";

    private static final String DEFAULT_HOSTILES = "AAAAAAAAAA";
    private static final String UPDATED_HOSTILES = "BBBBBBBBBB";

    private static final String DEFAULT_STATUS = "AAAAAAAAAA";
    private static final String UPDATED_STATUS = "BBBBBBBBBB";

    @Autowired
    private LocationRepository locationRepository;

    @Autowired
    private LocationService locationService;

    @Autowired
    private MappingJackson2HttpMessageConverter jacksonMessageConverter;

    @Autowired
    private PageableHandlerMethodArgumentResolver pageableArgumentResolver;

    @Autowired
    private ExceptionTranslator exceptionTranslator;

    @Autowired
    private Validator validator;

    private MockMvc restLocationMockMvc;

    private Location location;

    @BeforeEach
    public void setup() {
        MockitoAnnotations.initMocks(this);
        final LocationResource locationResource = new LocationResource(locationService);
        this.restLocationMockMvc = MockMvcBuilders.standaloneSetup(locationResource)
            .setCustomArgumentResolvers(pageableArgumentResolver)
            .setControllerAdvice(exceptionTranslator)
            .setConversionService(createFormattingConversionService())
            .setMessageConverters(jacksonMessageConverter)
            .setValidator(validator).build();
    }

    /**
     * Create an entity for this test.
     *
     * This is a static method, as tests for other entities might also need it,
     * if they test an entity which requires the current entity.
     */
    public static Location createEntity() {
        Location location = new Location()
            .seaQuadrant(DEFAULT_SEA_QUADRANT)
            .friendlys(DEFAULT_FRIENDLYS)
            .hostiles(DEFAULT_HOSTILES)
            .status(DEFAULT_STATUS);
        return location;
    }
    /**
     * Create an updated entity for this test.
     *
     * This is a static method, as tests for other entities might also need it,
     * if they test an entity which requires the current entity.
     */
    public static Location createUpdatedEntity() {
        Location location = new Location()
            .seaQuadrant(UPDATED_SEA_QUADRANT)
            .friendlys(UPDATED_FRIENDLYS)
            .hostiles(UPDATED_HOSTILES)
            .status(UPDATED_STATUS);
        return location;
    }

    @BeforeEach
    public void initTest() {
        locationRepository.deleteAll();
        location = createEntity();
    }

    @Test
    public void createLocation() throws Exception {
        int databaseSizeBeforeCreate = locationRepository.findAll().size();

        // Create the Location
        restLocationMockMvc.perform(post("/api/locations")
            .contentType(TestUtil.APPLICATION_JSON_UTF8)
            .content(TestUtil.convertObjectToJsonBytes(location)))
            .andExpect(status().isCreated());

        // Validate the Location in the database
        List<Location> locationList = locationRepository.findAll();
        assertThat(locationList).hasSize(databaseSizeBeforeCreate + 1);
        Location testLocation = locationList.get(locationList.size() - 1);
        assertThat(testLocation.getSeaQuadrant()).isEqualTo(DEFAULT_SEA_QUADRANT);
        assertThat(testLocation.getFriendlys()).isEqualTo(DEFAULT_FRIENDLYS);
        assertThat(testLocation.getHostiles()).isEqualTo(DEFAULT_HOSTILES);
        assertThat(testLocation.getStatus()).isEqualTo(DEFAULT_STATUS);
    }

    @Test
    public void createLocationWithExistingId() throws Exception {
        int databaseSizeBeforeCreate = locationRepository.findAll().size();

        // Create the Location with an existing ID
        location.setId("existing_id");

        // An entity with an existing ID cannot be created, so this API call must fail
        restLocationMockMvc.perform(post("/api/locations")
            .contentType(TestUtil.APPLICATION_JSON_UTF8)
            .content(TestUtil.convertObjectToJsonBytes(location)))
            .andExpect(status().isBadRequest());

        // Validate the Location in the database
        List<Location> locationList = locationRepository.findAll();
        assertThat(locationList).hasSize(databaseSizeBeforeCreate);
    }


    @Test
    public void getAllLocations() throws Exception {
        // Initialize the database
        locationRepository.save(location);

        // Get all the locationList
        restLocationMockMvc.perform(get("/api/locations?sort=id,desc"))
            .andExpect(status().isOk())
            .andExpect(content().contentType(MediaType.APPLICATION_JSON_UTF8_VALUE))
            .andExpect(jsonPath("$.[*].id").value(hasItem(location.getId())))
            .andExpect(jsonPath("$.[*].seaQuadrant").value(hasItem(DEFAULT_SEA_QUADRANT.toString())))
            .andExpect(jsonPath("$.[*].friendlys").value(hasItem(DEFAULT_FRIENDLYS.toString())))
            .andExpect(jsonPath("$.[*].hostiles").value(hasItem(DEFAULT_HOSTILES.toString())))
            .andExpect(jsonPath("$.[*].status").value(hasItem(DEFAULT_STATUS.toString())));
    }
    
    @Test
    public void getLocation() throws Exception {
        // Initialize the database
        locationRepository.save(location);

        // Get the location
        restLocationMockMvc.perform(get("/api/locations/{id}", location.getId()))
            .andExpect(status().isOk())
            .andExpect(content().contentType(MediaType.APPLICATION_JSON_UTF8_VALUE))
            .andExpect(jsonPath("$.id").value(location.getId()))
            .andExpect(jsonPath("$.seaQuadrant").value(DEFAULT_SEA_QUADRANT.toString()))
            .andExpect(jsonPath("$.friendlys").value(DEFAULT_FRIENDLYS.toString()))
            .andExpect(jsonPath("$.hostiles").value(DEFAULT_HOSTILES.toString()))
            .andExpect(jsonPath("$.status").value(DEFAULT_STATUS.toString()));
    }

    @Test
    public void getNonExistingLocation() throws Exception {
        // Get the location
        restLocationMockMvc.perform(get("/api/locations/{id}", Long.MAX_VALUE))
            .andExpect(status().isNotFound());
    }

    @Test
    public void updateLocation() throws Exception {
        // Initialize the database
        locationService.save(location);

        int databaseSizeBeforeUpdate = locationRepository.findAll().size();

        // Update the location
        Location updatedLocation = locationRepository.findById(location.getId()).get();
        updatedLocation
            .seaQuadrant(UPDATED_SEA_QUADRANT)
            .friendlys(UPDATED_FRIENDLYS)
            .hostiles(UPDATED_HOSTILES)
            .status(UPDATED_STATUS);

        restLocationMockMvc.perform(put("/api/locations")
            .contentType(TestUtil.APPLICATION_JSON_UTF8)
            .content(TestUtil.convertObjectToJsonBytes(updatedLocation)))
            .andExpect(status().isOk());

        // Validate the Location in the database
        List<Location> locationList = locationRepository.findAll();
        assertThat(locationList).hasSize(databaseSizeBeforeUpdate);
        Location testLocation = locationList.get(locationList.size() - 1);
        assertThat(testLocation.getSeaQuadrant()).isEqualTo(UPDATED_SEA_QUADRANT);
        assertThat(testLocation.getFriendlys()).isEqualTo(UPDATED_FRIENDLYS);
        assertThat(testLocation.getHostiles()).isEqualTo(UPDATED_HOSTILES);
        assertThat(testLocation.getStatus()).isEqualTo(UPDATED_STATUS);
    }

    @Test
    public void updateNonExistingLocation() throws Exception {
        int databaseSizeBeforeUpdate = locationRepository.findAll().size();

        // Create the Location

        // If the entity doesn't have an ID, it will throw BadRequestAlertException
        restLocationMockMvc.perform(put("/api/locations")
            .contentType(TestUtil.APPLICATION_JSON_UTF8)
            .content(TestUtil.convertObjectToJsonBytes(location)))
            .andExpect(status().isBadRequest());

        // Validate the Location in the database
        List<Location> locationList = locationRepository.findAll();
        assertThat(locationList).hasSize(databaseSizeBeforeUpdate);
    }

    @Test
    public void deleteLocation() throws Exception {
        // Initialize the database
        locationService.save(location);

        int databaseSizeBeforeDelete = locationRepository.findAll().size();

        // Delete the location
        restLocationMockMvc.perform(delete("/api/locations/{id}", location.getId())
            .accept(TestUtil.APPLICATION_JSON_UTF8))
            .andExpect(status().isNoContent());

        // Validate the database contains one less item
        List<Location> locationList = locationRepository.findAll();
        assertThat(locationList).hasSize(databaseSizeBeforeDelete - 1);
    }

    @Test
    public void equalsVerifier() throws Exception {
        TestUtil.equalsVerifier(Location.class);
        Location location1 = new Location();
        location1.setId("id1");
        Location location2 = new Location();
        location2.setId(location1.getId());
        assertThat(location1).isEqualTo(location2);
        location2.setId("id2");
        assertThat(location1).isNotEqualTo(location2);
        location1.setId(null);
        assertThat(location1).isNotEqualTo(location2);
    }
}
