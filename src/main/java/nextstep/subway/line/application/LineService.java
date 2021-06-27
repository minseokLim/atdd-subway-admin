package nextstep.subway.line.application;

import nextstep.subway.common.exceptions.NotFoundException;
import nextstep.subway.line.domain.Line;
import nextstep.subway.line.domain.LineRepository;
import nextstep.subway.line.dto.LineRequest;
import nextstep.subway.line.dto.LineResponse;
import nextstep.subway.station.application.StationService;
import nextstep.subway.station.domain.Section;
import nextstep.subway.station.domain.Station;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;
import java.util.stream.Collectors;


@Service
@Transactional
public class LineService {
    private StationService stationService;
    private LineRepository lineRepository;

    public LineService(LineRepository lineRepository, StationService stationService) {
        this.lineRepository = lineRepository;
        this.stationService = stationService;
    }

    public LineResponse saveLine(LineRequest request) {
        Station upStation = stationService.findById(request.getUpStationId())
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.BAD_REQUEST,"invalid station id: "+ request.getUpStationId()));
        Station downStation = stationService.findById(request.getDownStationId())
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.BAD_REQUEST,"invalid station id: "+ request.getDownStationId()));
        Section downSection = Section.of(downStation,"0");
        Section upSection = Section.of(upStation, request.getDistance());

        Line persistLine = lineRepository.save(request.toLine(upSection,downSection));
        return LineResponse.of(persistLine);
    }

    public List<LineResponse> findAll() {
        return lineRepository.findAll().stream()
                .map(LineResponse::of).collect(Collectors.toList());
    }

    public LineResponse findById(Long id) {
        return lineRepository.findById(id)
                .map(LineResponse::of)
                .orElseThrow(() -> new NotFoundException("invalid " + id));
    }

    public void updateLine(Long id, Line newLine) {
        Line line = lineRepository.findById(id)
                .orElseThrow(() -> new NotFoundException("invalid "+id));
        line.update(newLine);
    }

    public void deleteLine(Long id) {
        Line line = lineRepository.findById(id)
                .orElseThrow(() -> new NotFoundException("invalid "+id));
        lineRepository.delete(line);
    }
}
