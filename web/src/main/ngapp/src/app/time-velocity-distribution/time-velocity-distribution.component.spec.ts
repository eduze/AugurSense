import { async, ComponentFixture, TestBed } from '@angular/core/testing';

import { TimeVelocityDistributionComponent } from './time-velocity-distribution.component';

describe('TimeVelocityDistributionComponent', () => {
  let component: TimeVelocityDistributionComponent;
  let fixture: ComponentFixture<TimeVelocityDistributionComponent>;

  beforeEach(async(() => {
    TestBed.configureTestingModule({
      declarations: [ TimeVelocityDistributionComponent ]
    })
    .compileComponents();
  }));

  beforeEach(() => {
    fixture = TestBed.createComponent(TimeVelocityDistributionComponent);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
