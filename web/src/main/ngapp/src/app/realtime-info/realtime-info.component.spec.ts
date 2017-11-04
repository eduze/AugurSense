import { async, ComponentFixture, TestBed } from '@angular/core/testing';

import { RealtimeInfoComponent } from './realtime-info.component';

describe('RealtimeInfoComponent', () => {
  let component: RealtimeInfoComponent;
  let fixture: ComponentFixture<RealtimeInfoComponent>;

  beforeEach(async(() => {
    TestBed.configureTestingModule({
      declarations: [ RealtimeInfoComponent ]
    })
    .compileComponents();
  }));

  beforeEach(() => {
    fixture = TestBed.createComponent(RealtimeInfoComponent);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
