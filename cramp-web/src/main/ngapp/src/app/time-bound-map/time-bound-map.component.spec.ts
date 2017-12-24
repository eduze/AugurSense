import { async, ComponentFixture, TestBed } from '@angular/core/testing';

import { TimeBoundMapComponent } from './time-bound-map.component';

describe('TimeBoundMapComponent', () => {
  let component: TimeBoundMapComponent;
  let fixture: ComponentFixture<TimeBoundMapComponent>;

  beforeEach(async(() => {
    TestBed.configureTestingModule({
      declarations: [ TimeBoundMapComponent ]
    })
    .compileComponents();
  }));

  beforeEach(() => {
    fixture = TestBed.createComponent(TimeBoundMapComponent);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
