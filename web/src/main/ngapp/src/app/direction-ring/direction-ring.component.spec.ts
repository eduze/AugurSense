import { async, ComponentFixture, TestBed } from '@angular/core/testing';

import { DirectionRingComponent } from './direction-ring.component';

describe('DirectionRingComponent', () => {
  let component: DirectionRingComponent;
  let fixture: ComponentFixture<DirectionRingComponent>;

  beforeEach(async(() => {
    TestBed.configureTestingModule({
      declarations: [ DirectionRingComponent ]
    })
    .compileComponents();
  }));

  beforeEach(() => {
    fixture = TestBed.createComponent(DirectionRingComponent);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
